# Implementation Plan - Fix Missing Resource Error

The project fails to build because `fragment_automatic.xml` references a non-existent drawable resource: `@drawable/img_mapa_referencial`.

## Proposed Changes

### [Component Name] Layouts

#### [MODIFY] [fragment_automatic.xml](file:///D:/Desarrollo-Software/Proyectos%20Android/factorEscala/app/src/main/res/layout/fragment_automatic.xml)

- Remove the `android:src="@drawable/img_mapa_referencial"` attribute from the `ImageView` with ID `img_minimapa`.
- Add a placeholder color or leave it empty to allow the build to proceed. Since it's a referential map image inside a CardView that is hidden by default, removing the source is the safest way to fix the build error without introducing incorrect assets.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:assembleDebug` to verify that the resource linking error is resolved.

### Manual Verification
- Open the layout in the IDE editor to ensure no other errors are present.
