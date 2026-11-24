# CelesTech EMI Fix

An unofficial, client-side mod that makes **CelesTech** recipes show up in **EMI** again, and makes them easy to read.

- Minecraft 1.21.1, NeoForge
- Made for CelesTech 0.1.6-alpha (mod version 0.503), EMI 1.1.24+1.21.1 and JEI 19.64.0.455
- Not affiliated with CelesTech or its author

> This mod was written with the help of an AI assistant (Claude). All of it is in
> [`src/main/java`](src/main/java); please read it before you trust the jar.

## Download

Get `celestech-emi-fix-<version>.jar` from [Releases](https://github.com/anoubios/celestech-emi-fix/releases/latest)
and put it in your `mods` folder next to CelesTech, JEI and EMI. It is needed on the client only. Keep only one
version of it in the folder.

## What it does

- **Shows CelesTech recipes in EMI.** CelesTech's own JEI integration is switched off by EMI (see below); this mod
  turns it back on, so all 15 CelesTech recipe categories appear in EMI.
- **Enlarges CelesTech recipes.** On CelesTech tabs each recipe gets its own page and is zoomed to fill the window, so
  its small text is readable. The zoom adapts to your screen and GUI scale and prefers sizes that keep CelesTech's
  thin lines sharp. Clicks, hover, tooltips and R/U on enlarged slots work as usual.
- **CelesTech style.** On CelesTech tabs the whole EMI recipe window (frame, tabs, title bars, arrows, recipe
  buttons, workstation slot) is drawn in CelesTech's colors. Tabs from other mods keep EMI's normal look.
- **Machine tab first.** The block, item and fluid tag tabs move to the end, so opening recipes for a machine shows
  the machine's tab first.
- **Tidy panels.** Fluid tanks get a little space below the panel's top edge, machine animations that reach slightly
  past the panel are kept inside it, and the recipe buttons sit centered in a side panel of their own.
- **Quieter log.** EMI 1.1.24 logs "Error rendering tooltip" when the mouse is over a screen while JEI reloads; that
  lookup is skipped until JEI is back.

Settings are in game under Mods > CelesTech EMI Fix > Config (or `config/jeict-client.toml`): the style can be turned
off, and the largest zoom changed (1.0 keeps EMI's normal size).

The EMI fix switches itself off when it is not needed: without EMI or JEI, or once a CelesTech version fixes the issue.
To check it, look in `logs/latest.log` for `[CelesTechEmiFix/]: Bridge active for CelesTech ...`; if it stays off,
the log says why in a line starting with `Bridge inactive:`.

## Why CelesTech recipes are missing in EMI

1. CelesTech ships a complete JEI plugin, `com.crist.celestech.integracion.PluginJei` (UID `celestech:jei`).
2. It also ships an EMI plugin, `com.crist.celestech.integracion.PluginEmi`, which only reports the size of CelesTech's
   custom screens. It adds no categories or recipes.
3. EMI treats every mod with an EMI plugin as having native EMI support. Its mixin
   `dev.emi.emi.mixin.jei.PluginCallerMixin` (into JEI's `PluginCaller`) then skips every JEI plugin of that mod, and
   JEMI skips that mod's JEI categories.
4. So `PluginJei` never runs and EMI has nothing to show. With JEI alone, nothing skips it.

This mod removes `PluginEmi` from the lists EMI gets from `dev.emi.emi.platform.EmiAgnos`, runs it under its own mod
id instead (so EMI still knows CelesTech's screen sizes), and only does so while CelesTech's only EMI plugin is that
screen-size class.

## For the CelesTech developers

Either change fixes the missing recipes upstream:

1. Remove the `@EmiEntrypoint` `PluginEmi`. EMI then runs `PluginJei` through JEMI. The generic JEI GUI handler for
   `PantallaFina` already returns `areaReal()` from `getGuiExtraAreas`, and JEMI passes JEI exclusion areas to EMI.
2. Keep an EMI plugin, but register the recipes natively in EMI as well.

## Is the jar safe?

- Release jars are built by GitHub Actions from the tagged source ([workflow](.github/workflows/release.yml)). Each
  release lists the jar's SHA-256 and links the build log.
- Builds are reproducible: run `./gradlew build` with JDK 21 and compare the SHA-256 of
  `build/libs/celestech-emi-fix-<version>.jar` with the release.
- The mod changes nothing outside EMI's recipe screen: no files besides its own config, no network access, no world data.

## Building

Run `./gradlew build` (JDK 21). EMI is a compile-only dependency from the TerraformersMC Maven and is not bundled.

## License

MIT, see [LICENSE](LICENSE). CelesTech, EMI and JEI belong to their authors; this project contains none of their code.
`TEMPLATE_LICENSE.txt` covers the NeoForge MDK template files this project started from.
