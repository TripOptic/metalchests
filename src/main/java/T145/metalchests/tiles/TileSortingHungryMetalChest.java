/*******************************************************************************
 * Copyright 2018 T145
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
package T145.metalchests.tiles;

import T145.metalchests.api.ItemsMC;
import T145.metalchests.api.immutable.ChestType;
import T145.metalchests.api.immutable.ChestUpgrade;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class TileSortingHungryMetalChest extends TileSortingMetalChest {

    public TileSortingHungryMetalChest(ChestType chestType) {
        super(chestType);
    }

    public TileSortingHungryMetalChest() {
        super();
    }

    @Override
    public boolean canApplyUpgrade(ChestUpgrade upgrade, TileEntity chest, ItemStack upgradeStack) {
        return chest instanceof TileSortingHungryMetalChest && upgradeStack.getItem().getRegistryName().equals(ItemsMC.HUNGRY_CHEST_UPGRADE.getRegistryName());
    }

    @Override
    public boolean receiveClientEvent(int id, int data) {
        switch (id) {
        case 2:
            if (lidAngle < data / 10F) {
                lidAngle = data / 10F;
            }
            return true;
        default:
            return super.receiveClientEvent(id, data);
        }
    }

    @Override
    public String getTranslationKey() {
        return "tile.metalchests:sorting_hungry_metal_chest." + chestType.getName() + ".name";
    }
}
