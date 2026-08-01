# Implementation Plan - Mini Map Toggle in Automatic Mode

Add a "Mapa" toggle switch to the main header in the "Automático" tab and a `CardView` to hold a future minimap. The visibility of this card will be controlled by the switch.

## User Review Required

> [!NOTE]
> The minimap area will be empty for now as requested. Activating the switch will show a dark card, and deactivating it will collapse the space completely (`View.GONE`).

## Proposed Changes

### [app] - Layouts

#### [MODIFY] [fragment_automatic.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/fragment_automatic.xml)
- Wrap the "SISTEMA DE REFERENCIA WGS84" `TextView` in a horizontal `LinearLayout`.
- Add a `com.google.android.material.materialswitch.MaterialSwitch` to the right with the text "Mapa".
- Add a `androidx.cardview.widget.CardView` (ID: `@+id/card_mapa`) below the title line but above the GPS data row.
- Set `android:visibility="gone"` by default on `card_mapa`.
- Style the `CardView` with `8dp` radius and a fixed height (e.g., `200dp`) to act as a placeholder.

### [app] - Java Code

#### [MODIFY] [AutomaticFragment.java](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/solucionesit/factorcombinado/AutomaticFragment.java)
- Define `MaterialSwitch switchMapa` and `CardView cardMapa` member variables.
- In `onViewCreated`:
    - Initialize both views.
    - Set a `setOnCheckedChangeListener` to `switchMapa`.
    - Logic: `cardMapa.setVisibility(isChecked ? View.VISIBLE : View.GONE)`.

## Verification Plan

### Automated Tests
1. Run `gradlew clean assembleDebug` to ensure all new XML elements (Switch/CardView) are correctly linked.

### Manual Verification
1. Open the app and go to the "Automático" tab.
2. Verify the "Mapa" switch appears next to the title.
3. Toggle the switch ON and verify an empty card appears above the coordinates.
4. Toggle the switch OFF and verify the card disappears and coordinates move back up.
