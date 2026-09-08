# Walkthrough - UI Tweak: Map Clean Button Color

I have updated the "Clean Map" button color in the map view to a soft red as requested.

## Changes Made

### Layouts

#### [fragment_map.xml](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/res/layout/fragment_map.xml)
- Changed `app:backgroundTint` of `fab_clear_map` from `@color/accent_orange` to `@color/accent_red_soft`.

### Resources

#### [colors.xml (Night)](file:///D:/Desarrollo-Software/Proyectos Android/factorEscala/app/src/main/res/values-night/colors.xml)
- Added `accent_red_soft` definition to ensure consistency in Dark Mode.

## Verification Results

### Visual Verification
- The button `fab_clear_map` now uses the `#FF7070` (accent_red_soft) color.
