/*******************************************************************************
 * Copyright 2019 T145
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package T145.metalchests.entities;

import javax.annotation.Nullable;

import T145.metalchests.api.BlocksMC;
import T145.metalchests.api.ItemsMC;
import T145.metalchests.api.chests.IMetalChest;
import T145.metalchests.api.immutable.ChestType;
import T145.metalchests.api.immutable.ChestUpgrade;
import T145.metalchests.api.immutable.RegistryMC;
import T145.metalchests.core.MetalChests;
import T145.metalchests.core.ModSupport;
import T145.metalchests.items.ItemChestUpgrade;
import cofh.core.init.CoreEnchantments;
import mods.railcraft.api.carts.IItemCart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

@Optional.Interface(modid = ModSupport.RAILCRAFT_MOD_ID, iface = ModSupport.IFACE_ITEM_CART, striprefs = true)
public class EntityMinecartMetalChest extends EntityMinecart implements IMetalChest, IItemCart {

	private static final DataParameter<ChestType> CHEST_TYPE = EntityDataManager.<ChestType>createKey(EntityMinecart.class, MetalChests.CHEST_TYPE);
	private final ItemStackHandler inventory = new ItemStackHandler(getChestType().getInventorySize());
	private byte enchantLevel;

	public EntityMinecartMetalChest(World world) {
		super(world);
	}

	public EntityMinecartMetalChest(World world, double x, double y, double z) {
		super(world, x, y, z);
	}

	@Override
	public ItemStackHandler getInventory() {
		return inventory;
	}

	@Override
	public void setInventory(IItemHandler stacks) {
		for (int slot = 0; slot < stacks.getSlots(); ++slot) {
			if (slot < getChestType().getInventorySize()) {
				inventory.setStackInSlot(slot, stacks.getStackInSlot(slot));
			}
		}
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return !this.isDead && player.getDistanceSq(this) <= 64.0D;
	}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) {}

	@Override
	public ChestType getChestType() {
		return dataManager.get(CHEST_TYPE);
	}

	@Override
	public void setChestType(ChestType type) {
		dataManager.set(CHEST_TYPE, type);
	}

	@Override
	public EnumFacing getFront() {
		return EnumFacing.EAST;
	}

	@Override
	public void setFront(EnumFacing front) {}

	@Override
	public byte getEnchantLevel() {
		return enchantLevel;
	}

	@Override
	public void setEnchantLevel(byte enchantLevel) {
		this.enchantLevel = enchantLevel;
	}

	@Override
	protected void entityInit() {
		super.entityInit();
		dataManager.register(CHEST_TYPE, ChestType.OBSIDIAN);
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound tag) {
		super.writeEntityToNBT(tag);
		tag.setString("Type", getChestType().toString());
		tag.setTag("Inventory", inventory.serializeNBT());
		tag.setByte("EnchantLevel", enchantLevel);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound tag) {
		super.readEntityFromNBT(tag);
		this.setChestType(ChestType.valueOf(tag.getString("Type")));
		inventory.deserializeNBT(tag.getCompoundTag("Inventory"));
		this.setEnchantLevel(tag.getByte("EnchantLevel"));
	}

	private void dropItems() {
		for (int i = 0; i < inventory.getSlots(); ++i) {
			ItemStack slotStack = inventory.getStackInSlot(i);

			if (!slotStack.isEmpty()) {
				InventoryHelper.spawnItemStack(world, posX, posY, posZ, slotStack);
			}
		}
	}

	@Override
	public void killMinecart(DamageSource source) {
		super.killMinecart(source);

		if (world.getGameRules().getBoolean("doEntityDrops")) {
			ItemStack stack = new ItemStack(BlocksMC.METAL_CHEST, 1, getChestType().ordinal());

			if (ModSupport.hasThermalExpansion()) {
				NBTTagCompound tag = new NBTTagCompound();

				if (enchantLevel > 0) {
					CoreEnchantments.addEnchantment(tag, CoreEnchantments.holding, enchantLevel);
				}

				if (enchantLevel >= getChestType().getHoldingEnchantBound()) {
					tag.setTag("Inventory", inventory.serializeNBT());
				} else {
					dropItems();
				}

				if (!tag.isEmpty()) {
					stack.setTagCompound(tag);
				}
			} else {
				dropItems();
			}

			entityDropItem(stack, 0.0F);
		}
	}

	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
		if (super.processInitialInteract(player, hand) || world.isRemote) {
			return true;
		}

		ItemStack stack = player.getHeldItem(hand);

		if (stack.getItem() instanceof ItemChestUpgrade) {
			ChestUpgrade upgrade = ChestUpgrade.byMetadata(stack.getItemDamage());

			if (getChestType() == upgrade.getBase()) {
				setChestType(upgrade.getUpgrade());
				setInventory(inventory);

				if (!player.capabilities.isCreativeMode) {
					stack.shrink(1);
				}

				player.world.playSound(null, player.getPosition(), SoundEvents.BLOCK_ANVIL_PLACE, SoundCategory.PLAYERS, 0.4F, 0.8F);
				return true;
			}
		}

		player.openGui(RegistryMC.MOD_ID, hashCode(), world, 0, 0, 0);
		return true;
	}

	@Override
	protected void applyDrag() {
		float f = 0.98F;

		int i = 15 - ItemHandlerHelper.calcRedstoneFromInventory(inventory);
		f += (float) i * 0.001F;

		this.motionX *= (double) f;
		this.motionY *= 0.0D;
		this.motionZ *= (double) f;
	}

	@Override
	public ItemStack getCartItem() {
		return new ItemStack(ItemsMC.MINECART_METAL_CHEST, 1, getChestType().ordinal());
	}

	@Override
	public EntityMinecart.Type getType() {
		return EntityMinecart.Type.CHEST;
	}

	@Override
	public IBlockState getDefaultDisplayTile() {
		return BlocksMC.METAL_CHEST.getDefaultState().withProperty(IMetalChest.VARIANT, getChestType());
	}

	@Override
	public int getDefaultDisplayTileOffset() {
		return 8;
	}

	@Override
	public String getName() {
		if (hasCustomName()) {
			return getCustomNameTag();
		} else {
			return I18n.format(String.format("item.metalchests:minecart_metal_chest.%s.name", getChestType().getName()));
		}
	}

	@Override
	@Nullable
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}

	@Optional.Method(modid = ModSupport.RAILCRAFT_MOD_ID)
	@Override
	public boolean canPassItemRequests() {
		return true;
	}

	@Optional.Method(modid = ModSupport.RAILCRAFT_MOD_ID)
	@Override
	public boolean canAcceptPushedItem(EntityMinecart requester, ItemStack stack) {
		return !ItemHandlerHelper.insertItemStacked(inventory, stack, true).isEmpty();
	}

	@Optional.Method(modid = ModSupport.RAILCRAFT_MOD_ID)
	@Override
	public boolean canProvidePulledItem(EntityMinecart requester, ItemStack stack) {
		ItemStack result = ItemStack.EMPTY;

		for (int i = 0; i < inventory.getSlots(); ++i) {
			ItemStack extractedStack = inventory.extractItem(i, stack.getCount(), true);

			if (ItemStack.areItemStacksEqual(extractedStack, stack)) {
				result = extractedStack;
			}
		}

		return !result.isEmpty();
	}
}
