# CelesTech EMI Fix

An unofficial, temporary client-side mod that makes **CelesTech** recipes show up in **EMI** again.

- Minecraft 1.21.1, NeoForge
- Made for CelesTech 0.1.6-alpha (mod version 0.503), EMI 1.1.24+1.21.1 and JEI 19.64.0.455
- Not affiliated with CelesTech or its author. Remove it once CelesTech ships an official fix.

> This mod was written with the help of an AI assistant (Claude). It is five small classes in
> [`src/main/java`](src/main/java). Please read them before you trust the jar; it takes a few minutes.

## Download

Get `celestech-emi-fix-<version>.jar` from [Releases](https://github.com/anoubios/celestech-emi-fix/releases/latest)
and put it in your `mods` folder next to CelesTech, JEI and EMI. It is needed on the client only.

## The problem

With JEI and EMI installed together, none of CelesTech's own recipe categories appear: Fabricator, Foundry, Shipyard,
the processing machines, implant table, condenser, cannon, supernova, multiblocks and gas processing. Only vanilla
crafting and smelting recipes of CelesTech items are visible.

## Why it happens

1. CelesTech ships a complete JEI plugin, `com.crist.celestech.integracion.PluginJei` (UID `celestech:jei`), with
   15 recipe categories, catalysts, ore info pages and GUI handlers.
2. It also ships an EMI plugin, `com.crist.celestech.integracion.PluginEmi`. That plugin only registers a screen-bounds
   provider for CelesTech's custom screens (`PantallaFina`). It adds no categories or recipes.
3. EMI treats every mod that has an EMI plugin as having native EMI support. EMI's mixin
   `dev.emi.emi.mixin.jei.PluginCallerMixin` (into JEI's `mezz.jei.library.load.PluginCaller`) then drops every call to
   a JEI plugin whose UID namespace belongs to such a mod, and JEMI skips that mod's JEI categories
   ("Skipping recipe category ... because mod is already handled").
4. So `PluginJei` never runs: JEI registers no `celestech:*` categories and EMI has nothing to import. In an affected
   game, `config/jei/client/recipe-category-sort-order.ini` lists no `celestech:` category at all.

With JEI alone, nothing skips `PluginJei`.

## What this mod changes

- It removes CelesTech's `PluginEmi` from the lists EMI gets from `dev.emi.emi.platform.EmiAgnos#getPlugins` and
  `#getModsWithPlugins` (mixin [`EmiAgnosMixin`](src/main/java/com/jeict/mixin/EmiAgnosMixin.java)). EMI then lets
  `PluginJei` run and imports every CelesTech category through JEMI.
- It runs `PluginEmi` itself under this mod's id ([`JeiCtEmiPlugin`](src/main/java/com/jeict/emi/JeiCtEmiPlugin.java)),
  so EMI still knows the size of CelesTech's screens.
- It does nothing unless all of these hold ([`CelesTechBridge`](src/main/java/com/jeict/bridge/CelesTechBridge.java)):
  EMI and JEI are installed, CelesTech's only EMI plugin is that screen-bounds class, CelesTech still has its JEI
  plugin, and CelesTech registers no EMI recipes of its own. A CelesTech update that fixes the issue switches it off.

It changes nothing else: no files, settings, network access or world data.

To check that it works, look in `logs/latest.log` for:

```
[CelesTechEmiFix/]: Bridge active for CelesTech 0.503: ...
[CelesTechEmiFix/]: Hid com.crist.celestech.integracion.PluginEmi from EMI; ...
```

If it decided to stay off, the log says why in a line starting with `Bridge inactive:`.

## Is the jar safe?

- Release jars are built by GitHub Actions from the tagged source ([workflow](.github/workflows/release.yml)). Each
  release lists the jar's SHA-256 and links the build log.
- The jar is about 10 KB: the five classes above, `META-INF/neoforge.mods.toml` and `jeict.mixins.json`.
- Builds are reproducible: run `./gradlew build` with JDK 21 and compare
  `build/libs/celestech-emi-fix-<version>.jar` with the release.

## For the CelesTech developers

Either change fixes this upstream:

1. Remove the `@EmiEntrypoint` `PluginEmi`. EMI then runs `PluginJei` through JEMI. The screen area is mostly covered
   already: the generic JEI GUI handler for `PantallaFina` returns `areaReal()` from `getGuiExtraAreas`, and JEMI passes
   JEI exclusion areas on to EMI. JEMI 1.1.24 implements every JEI method `PluginJei`'s categories call.
2. Keep an EMI plugin, but register the recipes natively in EMI as well. EMI will not call `PluginJei` while
   `celestech` has an EMI entrypoint.

## Building

Run `./gradlew build` (JDK 21). EMI is a compile-only dependency from the TerraformersMC Maven and is not bundled.

## License

MIT, see [LICENSE](LICENSE). CelesTech, EMI and JEI belong to their authors; this project contains none of their code.
`TEMPLATE_LICENSE.txt` covers the NeoForge MDK template files this project started from.
