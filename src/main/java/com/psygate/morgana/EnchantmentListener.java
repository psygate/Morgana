/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.psygate.morgana;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class EnchantmentListener implements Listener {

    private final Configuration conf = Configuration.getInstance();

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void enchant(EnchantItemEvent ev) {
        Morgana.getPlugin(Morgana.class).getLogger().log(Level.INFO, "Enchant event.");
        Map<Enchantment, Integer> enchants = checkAndModifyEnchantments(ev.getEnchantsToAdd());
        ev.getEnchantsToAdd().clear();
        ev.getEnchantsToAdd().putAll(enchants);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.NORMAL)
    public void enchant(InventoryClickEvent ev) {
        Morgana.getPlugin(Morgana.class).getLogger().log(Level.INFO, "Anvil event.");

        if (ev.getClickedInventory() instanceof AnvilInventory) {
            AnvilInventory inv = (AnvilInventory) ev.getClickedInventory();
            ItemStack output = inv.getItem(inv.getSize() - 1);

            if (output == null || output.getType() == Material.AIR) {
                return;
            }

            Map<Enchantment, Integer> originalEnchantments = copy(output.getEnchantments());
            Map<Enchantment, Integer> enchants = checkAndModifyEnchantments(originalEnchantments);

            for (Enchantment key : originalEnchantments.keySet()) {
                output.removeEnchantment(key);
            }

            for (Map.Entry<Enchantment, Integer> en : enchants.entrySet()) {
                output.addEnchantment(en.getKey(), en.getValue());
            }
        }
    }

    private Map<Enchantment, Integer> checkAndModifyEnchantments(Map<Enchantment, Integer> enchantmap) {
        HashMap<Enchantment, Integer> alternatives = new HashMap<>();

        for (Map.Entry<Enchantment, Integer> en : enchantmap.entrySet()) {
            if (conf.isAllowed(en.getKey(), en.getValue())) {
                Morgana.getPlugin(Morgana.class).getLogger().log(Level.INFO, "Allowing enchantment: {0}({1})", new Object[]{en, en.getValue()});
                alternatives.put(en.getKey(), en.getValue());
            } else {
                Morgana.getPlugin(Morgana.class).getLogger().log(Level.INFO, "Removing enchantment: {0}({1})", new Object[]{en, en.getValue()});
                if (conf.hasAlternative(en.getKey(), en.getValue())) {
                    Morgana.getPlugin(Morgana.class).getLogger().log(Level.INFO, "Adding alternative: {0}({1})", new Object[]{en, en.getValue()});
                    alternatives.put(en.getKey(), conf.getAlternative(en.getKey(), en.getValue()));
                }
            }
        }

        return alternatives;
    }

    private Map<Enchantment, Integer> copy(Map<Enchantment, Integer> originalEnchantments) {
        HashMap<Enchantment, Integer> alternatives = new HashMap<>();
        alternatives.putAll(originalEnchantments);
        return alternatives;
    }
}
