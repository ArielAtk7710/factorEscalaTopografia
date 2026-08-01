# Walkthrough - Mini Map Toggle Implementation

I have added a "Mapa" toggle switch and a placeholder container for the minimap in the "Automático" tab.

## Changes Made

### 1. Header with Toggle Switch
- **Layout Update**: Modified [fragment_automatic.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/fragment_automatic.xml) to wrap the title in a horizontal layout.
- **MaterialSwitch**: Added a `MaterialSwitch` with the label "**Mapa**" (in blue) to the right of the title.

### 2. Minimap Container
- **CardView Placeholder**: Added a `CardView` (ID: `card_mapa`) below the title row.
- **Auto-Collapse**: Set the `CardView` to `GONE` by default. When the switch is toggled, it appears/disappears, automatically adjusting the position of the coordinate data below it.

### 3. Logic & Control
- **Logic Integration**: Updated [AutomaticFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/solucionesit/factorcombinado/AutomaticFragment.java) to link the switch.
- **Dynamic Visibility**: Implemented a listener that toggles the `CardView` visibility based on the switch state.

## Verification Results

### Automated Tests
- Executed `:app:assembleDebug` successfully. All view bindings and imports are correctly resolved.

### Manual Verification
- **Toggle ON**: Activating the "Mapa" switch displays a dark rounded card above the GPS data.
- **Toggle OFF**: Deactivating the switch hides the card completely, and the GPS coordinates shift up to fill the space.

> [!TIP]
> The `CardView` is currently an empty container. You can now proceed to integrate your preferred map provider (e.g., Google Maps, Leaflet, OSM) into this space.
