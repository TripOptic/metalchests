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

import T145.metalchests.blocks.BlockMetalTank.TankType;
import net.minecraft.nbt.NBTTagCompound;

public class TileMetalTank extends TileMod {

	private TankType type;

	public TileMetalTank(TankType type) {
		this.type = type;
	}

	public TileMetalTank() {
		this(TankType.BASE);
	}

	public TankType getType() {
		return type;
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {
		super.readFromNBT(tag);
		type = TankType.valueOf(tag.getString("Type"));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag) {
		tag = super.writeToNBT(tag);
		tag.setString("Type", type.toString());
		return tag;
	}
}
