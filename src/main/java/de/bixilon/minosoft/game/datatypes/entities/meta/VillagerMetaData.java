/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.game.datatypes.entities.meta;

import de.bixilon.minosoft.game.datatypes.entities.VillagerData;

public class VillagerMetaData extends AbstractMerchantMetaData {

    public VillagerMetaData(MetaDataHashMap sets, int protocolId) {
        super(sets, protocolId);
    }

    public VillagerData.VillagerProfessions getProfession() {
        final int defaultValue = VillagerData.VillagerProfessions.FARMER.getId(protocolId);
        if (protocolId < 57) {
            return VillagerData.VillagerProfessions.byId(sets.getInt(16, defaultValue), protocolId);
        }
        if (protocolId < 451) {
            return VillagerData.VillagerProfessions.byId(sets.getInt(super.getLastDataIndex() + 1, defaultValue), protocolId);
        }
        return getVillageData().getProfession();
    }

    public VillagerData.VillagerTypes getType() {
        return getVillageData().getType();
    }

    public VillagerData.VillagerLevels getLevel() {
        return getVillageData().getLevel();
    }

    public VillagerData getVillageData() {
        final VillagerData defaultValue = new VillagerData(VillagerData.VillagerTypes.PLAINS, VillagerData.VillagerProfessions.NONE, VillagerData.VillagerLevels.APPRENTICE);
        if (protocolId < 451) {
            return defaultValue;
        }
        return sets.getVillagerData(super.getLastDataIndex() + 1, defaultValue);
    }

    @Override
    protected int getLastDataIndex() {
        return super.getLastDataIndex() + 1;
    }
}