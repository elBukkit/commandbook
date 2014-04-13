package com.sk89q.commandbook.util.item.itemstack.implementations;

import com.sk89q.commandbook.util.item.itemstack.SerializableItemStack;
import org.bukkit.Color;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class SerializableItemStack_G1 implements SerializableItemStack {

    private Map<String, Object> map;

    public SerializableItemStack_G1(ItemStack itemStack) {
        map = itemStack.serialize();
        if (map.containsKey("meta")) {
            Map<String, Object> aMetaMap = new HashMap<String, Object>();
            aMetaMap.put("==", ConfigurationSerialization.getAlias(itemStack.getItemMeta().getClass()));
            for (Map.Entry<String, Object> entry : itemStack.getItemMeta().serialize().entrySet()) {

                String key = entry.getKey();
                Object o = entry.getValue();

                if (key.equals("custom-effects")) {
                    List<Map<String, Object>> potionEffects = new ArrayList<Map<String, Object>>();
                    for (PotionEffect effect : (List<PotionEffect>) o) {
                        Map<String, Object> aPotionEffectMap = new HashMap<String, Object>();
                        aPotionEffectMap.put("==", ConfigurationSerialization.getAlias(effect.getClass()));
                        for (Map.Entry<String, Object> aEntry : effect.serialize().entrySet()) {
                            aPotionEffectMap.put(aEntry.getKey(), aEntry.getValue());
                        }
                        potionEffects.add(aPotionEffectMap);
                    }
                    o = potionEffects;

                } else if (key.equals("color")) {
                    Map<String, Object> aColorMap = new HashMap<String, Object>();
                    //noinspection RedundantCast
                    aColorMap.put("==", ConfigurationSerialization.getAlias(((Color) o).getClass()));
                    for (Map.Entry<String, Object> aEntry : ((Color) o).serialize().entrySet()) {
                        aColorMap.put(aEntry.getKey(), aEntry.getValue());
                    }
                    o = aColorMap;

                }

                ByteArrayOutputStream bos;
                ObjectOutputStream oss = null;
                try {
                    bos = new ByteArrayOutputStream();
                    oss = new ObjectOutputStream(bos);
                    oss.writeObject(o);
                    oss.close();
                } catch (IOException ex) {
                    System.out.println("Error encountered processing key: " + key);
                    ex.printStackTrace();
                    continue;
                } finally {
                    if (oss != null) {
                        try {
                            oss.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                aMetaMap.put(key, o);
            }
            map.put("meta", aMetaMap);
        }
    }

    @Override
    public ItemStack toBukkit() {
        ItemStack stack = ItemStack.deserialize(map);
        ItemMeta meta = null;
        if (map.containsKey("meta")) {
            Object metaMap = map.get("meta");
            if (metaMap instanceof Map) {
                Map<String, Object> aMetaMap = new HashMap<String, Object>();
                for (Map.Entry<String, Object> entry : ((Map<String, Object>) metaMap).entrySet()) {
                    aMetaMap.put(entry.getKey(), entry.getValue());
                }

                if (aMetaMap.containsKey("custom-effects")) {
                    List<PotionEffect> potionEffects = new ArrayList<PotionEffect>();
                    for (Map<String, Object> entry : (List<Map<String, Object>>) aMetaMap.get("custom-effects")) {
                        potionEffects.add((PotionEffect) ConfigurationSerialization.deserializeObject(entry));
                    }
                    aMetaMap.put("custom-effects", potionEffects);
                }

                if (aMetaMap.containsKey("color")) {
                    aMetaMap.put("color", ConfigurationSerialization.deserializeObject((Map<String, Object>) aMetaMap.get("color")));
                }

                meta = (ItemMeta) ConfigurationSerialization.deserializeObject(aMetaMap);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }
}