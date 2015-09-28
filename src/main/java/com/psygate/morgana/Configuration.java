package com.psygate.morgana;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import org.bukkit.configuration.MemorySection;

/*
 The MIT License (MIT)

 Copyright (c) 2015 psygate (https://github.com/psygate)

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.

 */
/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class Configuration {

//    private final Set<Enchant> allowed;
    private final Map<Enchantment, SortedSet<Integer>> allowed;
    private static Configuration conf;
    private boolean downgrade;

    public static Configuration getInstance() {
        if (conf == null) {
            conf = new Configuration();
        }

        return conf;
    }

    private Configuration() {
        Morgana m = Morgana.getPlugin(Morgana.class);
        if (m.getConfig().getBoolean("whitelist")) {
            allowed = buildWhitelist();
        } else {
            allowed = buildBlacklist();
        }

        downgrade = m.getConfig().getBoolean("downgrade_enchantments");
        Morgana.getPlugin(Morgana.class).getLogger().log(Level.INFO, "Whitelisted enchantments: {0}", allowed);
    }

    public boolean isAllowed(Enchantment en, int level) {
        boolean ok = allowed.containsKey(en) && allowed.get(en).contains(level);
        Morgana.getPlugin(Morgana.class).getLogger().log(Level.INFO, "{0}({1}) allowed: {2}", new Object[]{en, level, ok});
        return ok;
    }

    public boolean hasAlternative(Enchantment en, int level) {
        return downgrade && allowed.containsKey(en) && !allowed.get(en).headSet(level).isEmpty();
    }

    public int getAlternative(Enchantment en, int level) {
        if (hasAlternative(en, level)) {
            int alt = allowed.get(en).headSet(level).last();
            Morgana.getPlugin(Morgana.class).getLogger().log(Level.INFO, "Switching enchantment {0}({1}) for {2} (Used set: {3})", new Object[]{en, level, alt, allowed.get(en).headSet(level)});
            return alt;
        } else {
            throw new IllegalArgumentException("No alternatives for " + en + "(" + level + ").");
        }
    }

    private Map<Enchantment, SortedSet<Integer>> buildWhitelist() {
        return loadConfigList();
    }

    private Map<Enchantment, SortedSet<Integer>> loadConfigList() {
        Map<Enchantment, SortedSet<Integer>> whitelist = new HashMap<>();

        Morgana m = Morgana.getPlugin(Morgana.class);
        MemorySection sec = (MemorySection) m.getConfig().get("enchantment");
        for (String key : sec.getValues(false).keySet()) {
            for (Integer level : (List<Integer>) sec.getList(key)) {
                Enchantment enc = Enchantment.getByName(key);
                whitelist.putIfAbsent(enc, new TreeSet<Integer>());
                whitelist.get(enc).add(level);
            }
        }

        return whitelist;
    }

    private Map<Enchantment, SortedSet<Integer>> buildCompleteList() {
        Map<Enchantment, SortedSet<Integer>> whitelist = new HashMap<>();

        for (Enchantment e : Enchantment.values()) {
            for (int i = e.getStartLevel(); i <= e.getMaxLevel(); i++) {
                whitelist.putIfAbsent(e, new TreeSet<Integer>());

                whitelist.get(e).add(i);
            }
        }

        return whitelist;
    }

    private Map<Enchantment, SortedSet<Integer>> buildBlacklist() {
        Map<Enchantment, SortedSet<Integer>> whitelist = buildCompleteList();
        Map<Enchantment, SortedSet<Integer>> blacklist = buildWhitelist();

        for (Enchantment e : blacklist.keySet()) {
            for (Integer i : blacklist.get(e)) {
                whitelist.get(e).remove(i);
            }
        }
        return whitelist;
    }

    private class Enchant {

        final int level;
        final Enchantment enchantment;

        public Enchant(int level, Enchantment enchantment) {
            this.level = level;
            this.enchantment = enchantment;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 97 * hash + this.level;
            hash = 97 * hash + Objects.hashCode(this.enchantment);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Enchant other = (Enchant) obj;
            if (this.level != other.level) {
                return false;
            }
            if (!Objects.equals(this.enchantment, other.enchantment)) {
                return false;
            }
            return true;
        }

    }
}
