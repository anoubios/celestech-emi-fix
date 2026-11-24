Unofficial, client-side fix that makes CelesTech recipes show up in EMI again and easy to read (Minecraft 1.21.1, NeoForge).

**New in 1.1.1**
- Recipe buttons (favorite, tree, fill) sit centered in their own side panel next to the enlarged recipe.
- Fluid tanks no longer touch the top edge of the machine panel.
- EMI no longer logs "Error rendering tooltip" (NullPointerException on JemiPlugin.runtime) while JEI reloads.

**New in 1.1.0**
- CelesTech recipes are enlarged to fill EMI's window, so their text is readable. The zoom adapts to your screen.
- On CelesTech tabs the whole recipe window uses CelesTech's style. Other mods' tabs look as usual.
- The machine tab opens first; tag tabs moved to the end.
- Machine animations stay inside the panel frame.
- Settings: Mods > CelesTech EMI Fix > Config (style on/off, largest zoom).

**Install:** put the jar in your `mods` folder next to CelesTech, JEI and EMI, and remove older versions of it. It is client-side only.

Made for CelesTech 0.1.6-alpha (mod version 0.503), EMI 1.1.24+1.21.1 and JEI 19.64.0.455. The [README](https://github.com/anoubios/celestech-emi-fix#readme) explains what it changes and why.
