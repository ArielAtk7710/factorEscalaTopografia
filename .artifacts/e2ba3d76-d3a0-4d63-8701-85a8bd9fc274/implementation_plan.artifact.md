# Implementation Plan - Uniform Button Formatting (Calculate & Clean) Across Calculation Views

This plan details the standardization of calculation and action buttons across all data input and calculation fragments (`Geodesic`, `Lambert`, `Distance Reduction`, and `Line Calculator`), matching the design and spacing format established in `fragment_manual.xml`.

## User Review Required

> [!IMPORTANT]
> - **New Clean Buttons**: Views such as Geodesic, Lambert, Distance Reduction, and Line Calculator currently only have a single "Calculate" button. This plan introduces a side-by-side "Clean" (`LIMPIAR`) button matching `fragment_manual.xml`.
> - **Fragment Logic Updates**: Adding a clean button (`btnClean` or similar ID) will require updating the corresponding fragment classes (`GeodesicFragment`, `LambertFragment`, `DistanceReductionFragment`, `LineCalculatorFragment`) to handle clearing input fields and results when clicked.

## Open Questions

- Should the string `@string/btn_clean` be used for all clean buttons? (Yes, it already exists in `strings.xml`).

## Proposed Changes

### Layout Files (UI Standardization)

#### [MODIFY] [fragment_geodesic.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/fragment_geodesic.xml)
- Replace single `btnCalculate` with a horizontal `LinearLayout` containing:
  - **CALCULATE**: `accent_orange` background, text primary, bold.
  - **LIMPIAR**: Outlined button with `state_error` stroke and text, bold.
  - Margins and spacing matching `fragment_manual.xml` (`layout_marginTop="12dp"`, `layout_weight="1"`, etc.).

#### [MODIFY] [fragment_lambert.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/fragment_lambert.xml)
- Replace single `btnCalculate` with the same side-by-side CALCULATE + LIMPIAR button layout.

#### [MODIFY] [fragment_distance_reduction.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/fragment_distance_reduction.xml)
- Replace single `btnCalculate` with the same side-by-side CALCULATE + LIMPIAR button layout.

### Fragment Logic (Kotlin Code)

#### [MODIFY] [GeodesicFragment.kt](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/GeodesicFragment.kt)
- Bind `btn_limpiar` (or `btnClean`) and implement clearing logic for input fields and results panel.

#### [MODIFY] [LambertFragment.kt](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/LambertFragment.kt)
- Bind `btn_limpiar` and implement clearing logic.

#### [MODIFY] [DistanceReductionFragment.kt](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/DistanceReductionFragment.kt)
- Bind `btn_limpiar` and implement clearing logic.

#### [MODIFY] [LineCalculatorFragment.kt](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/java/bo/com/factorcombinadotopo/LineCalculatorFragment.kt)
- Bind `btn_limpiar` and implement clearing logic.

## Verification Plan

### Automated Tests
- Build the app using `gradle_build` (`app:assembleDebug`) to ensure all XML layouts and Kotlin fragment bindings compile without errors.

### Manual Verification
- Ask the user to deploy and test each calculation screen (Manual, Geodesic, Lambert, Distance Reduction, Line Calculator) to verify that the buttons have identical colors (`accent_orange` and `state_error`), spacing (`12dp` top margin, `4dp` horizontal weight margins), and that clicking "Limpiar" successfully resets inputs and outputs.
