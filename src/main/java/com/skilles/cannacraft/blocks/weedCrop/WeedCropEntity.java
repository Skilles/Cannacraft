package com.skilles.cannacraft.blocks.weedCrop;

import com.skilles.cannacraft.registry.ModEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.CompoundTag;
import com.skilles.cannacraft.StrainType;

import java.util.HashMap;
import java.util.Map;

public class WeedCropEntity extends BlockEntity {

    public WeedCropEntity(BlockEntityType<?> blockEntityType) {
        super(ModEntities.WEED_CROP_ENTITY);
    }
    public WeedCropEntity() {
        this(ModEntities.WEED_CROP_ENTITY);
    }


    // Stores strain name and type. Contains setStrain() with either index or strainName parameters, getStrain(), and getType().
   /*private static class StrainType {
        int index = 0;
        String type;
        String strain;
        static int strainCount = 3; // Amount of strains
        Map<String, Integer> strainMap = new HashMap<String, Integer>();
        public StrainType() {
            // Goes through and maps each strain string to index
            for (int i = 0; strainCount - 1 < i; i++) {
                updateStrain(i);
                strainMap.put(this.strain, index); // ("OG Kush", 0)
            }
        }
        // Assigns name and type based on index
        private void updateStrain(int index){
            switch (index) {
                case 0:
                    strain = "OG Kush";
                    type = "Hybrid";
                case 1:
                    strain = "Purple Punch";
                    type = "Indica";
                case 2:
                    strain = "Chem Trix";
                    type = "Sativa";
                default:
                    strain = "Unknown";
                    type = "Unknown";
            }
        }
        // Set strain using index
        public void setStrain(int index) {
            updateStrain(index);
        }
        // Set strain using strain name
        public void setStrain(String strainName) {
           updateStrain(strainMap.get(strain));
        }

        public String getStrain() {
            return strain;
        }
        public String getType() {
           return type;
        }

   }*/

    private int thc = 0;


    StrainType strain = new StrainType();
    public void setStrain(int strainIndex) {
        this.strain.setStrain(strainIndex);
    }
    public void setStrain(String strainName) { this.strain.setStrain(strainName); }
    public void setThc(int thc) {
        this.thc = thc;
    }

    @Override
    public CompoundTag toTag(CompoundTag tag) {
        super.toTag(tag);
        tag.putString("Strain", strain.getStrain());
        tag.putInt("THC", thc);
        tag.putString("Type", strain.getType());
        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag compoundTag) {
        super.fromTag(state, compoundTag);
        strain.setStrain(compoundTag.getString("Strain")); //  set strain based on nbt tag
        thc = compoundTag.getInt("THC");
    }
}
