package com.jeict.bridge;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.fml.loading.moddiscovery.ModFileInfo;
import net.neoforged.neoforgespi.language.ModFileScanData;

import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Decides whether EMI should stop treating CelesTech as a mod with its own EMI support.
 *
 * <p>CelesTech 0.1.6-alpha ships an EMI plugin ({@code PluginEmi}) that only reports the size of its custom
 * screens. EMI treats every mod that has an EMI plugin as "handled": it skips that mod's JEI plugin and its JEI
 * recipe categories. CelesTech's recipe integration lives entirely in its JEI plugin ({@code PluginJei}), so with
 * EMI installed no CelesTech recipes show up at all.
 *
 * <p>The bridge turns on only while it can help: EMI and JEI are installed, CelesTech's only EMI plugin is that
 * bounds-only class, CelesTech still has a JEI plugin, and CelesTech has no EMI recipe classes of its own. Once a
 * CelesTech update changes any of that, the bridge switches itself off and leaves EMI alone.
 *
 * <p>This class must not reference EMI types: it is loaded even when EMI is not installed.
 */
public final class CelesTechBridge {
    public static final Logger LOGGER = LoggerFactory.getLogger("CelesTechEmiFix");

    public static final String CELESTECH_MOD_ID = "celestech";
    public static final String CELESTECH_EMI_PLUGIN = "com.crist.celestech.integracion.PluginEmi";

    private static final String EMI_ENTRYPOINT = "Ldev/emi/emi/api/EmiEntrypoint;";
    private static final String JEI_PLUGIN = "Lmezz/jei/api/JeiPlugin;";
    private static final Set<String> EMI_RECIPE_TYPES = Set.of(
            "dev/emi/emi/api/recipe/EmiRecipe",
            "dev/emi/emi/api/recipe/BasicEmiRecipe");

    private static final AtomicBoolean HIDE_LOGGED = new AtomicBoolean();
    private static volatile Boolean active;

    private CelesTechBridge() {}

    /** Whether CelesTech's bounds-only EMI plugin should be hidden from EMI. Decided once, then cached. */
    public static boolean isActive() {
        Boolean result = active;
        if (result == null) {
            synchronized (CelesTechBridge.class) {
                result = active;
                if (result == null) {
                    result = decide();
                    active = result;
                }
            }
        }
        return result;
    }

    /** Logs, once per game session, that EMI was actually given the filtered plugin list. */
    public static void noteHidden() {
        if (HIDE_LOGGED.compareAndSet(false, true)) {
            LOGGER.info("Hid {} from EMI; EMI will import CelesTech's JEI recipe categories instead", CELESTECH_EMI_PLUGIN);
        }
    }

    private static boolean decide() {
        try {
            LoadingModList mods = LoadingModList.get();
            if (mods.getModFileById("emi") == null) {
                return off("EMI is not installed, and CelesTech's JEI plugin works on its own");
            }
            if (mods.getModFileById("jei") == null) {
                return off("JEI is not installed, so there is no CelesTech JEI plugin for EMI to import");
            }
            ModFileInfo celestech = mods.getModFileById(CELESTECH_MOD_ID);
            if (celestech == null) {
                return off("CelesTech is not installed");
            }

            ModFileScanData scan = celestech.getFile().getScanResult();
            Set<String> emiPlugins = classesAnnotatedWith(scan, EMI_ENTRYPOINT);
            if (!emiPlugins.equals(Set.of(CELESTECH_EMI_PLUGIN))) {
                return off("CelesTech's EMI plugins are " + emiPlugins + ", not the known bounds-only one");
            }
            if (classesAnnotatedWith(scan, JEI_PLUGIN).isEmpty()) {
                return off("CelesTech has no JEI plugin any more");
            }
            if (hasOwnEmiRecipes(scan)) {
                return off("CelesTech now adds its own EMI recipes, so this addon is no longer needed");
            }

            LOGGER.info("Bridge active for CelesTech {}: its EMI plugin only reports screen sizes, "
                    + "so EMI will be allowed to import its JEI recipes", version(celestech));
            return true;
        } catch (RuntimeException | LinkageError e) {
            LOGGER.error("Could not inspect CelesTech, leaving EMI alone", e);
            return false;
        }
    }

    private static Set<String> classesAnnotatedWith(ModFileScanData scan, String annotationDescriptor) {
        return scan.getAnnotations().stream()
                .filter(a -> annotationDescriptor.equals(a.annotationType().getDescriptor()))
                .map(ModFileScanData.AnnotationData::memberName)
                .collect(Collectors.toSet());
    }

    private static boolean hasOwnEmiRecipes(ModFileScanData scan) {
        return scan.getClasses().stream().anyMatch(c ->
                (c.parent() != null && EMI_RECIPE_TYPES.contains(c.parent().getInternalName()))
                        || c.interfaces().stream().anyMatch(i -> EMI_RECIPE_TYPES.contains(i.getInternalName())));
    }

    private static String version(ModFileInfo file) {
        return file.getMods().stream()
                .filter(m -> CELESTECH_MOD_ID.equals(m.getModId()))
                .map(m -> m.getVersion().toString())
                .findFirst()
                .orElse("(unknown version)");
    }

    private static boolean off(String reason) {
        LOGGER.info("Bridge inactive: {}", reason);
        return false;
    }
}
