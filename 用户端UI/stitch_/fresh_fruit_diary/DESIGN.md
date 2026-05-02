---
name: Fresh Fruit Diary
colors:
  surface: '#fcf9f8'
  surface-dim: '#dcd9d9'
  surface-bright: '#fcf9f8'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f6f3f2'
  surface-container: '#f0eded'
  surface-container-high: '#eae7e7'
  surface-container-highest: '#e5e2e1'
  on-surface: '#1b1c1c'
  on-surface-variant: '#40493d'
  inverse-surface: '#303030'
  inverse-on-surface: '#f3f0ef'
  outline: '#707a6c'
  outline-variant: '#bfcaba'
  surface-tint: '#1b6d24'
  primary: '#0d631b'
  on-primary: '#ffffff'
  primary-container: '#2e7d32'
  on-primary-container: '#cbffc2'
  inverse-primary: '#88d982'
  secondary: '#3e6a00'
  on-secondary: '#ffffff'
  secondary-container: '#b9f474'
  on-secondary-container: '#437000'
  tertiary: '#ae0714'
  on-tertiary: '#ffffff'
  tertiary-container: '#d22a2a'
  on-tertiary-container: '#ffeeec'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#a3f69c'
  primary-fixed-dim: '#88d982'
  on-primary-fixed: '#002204'
  on-primary-fixed-variant: '#005312'
  secondary-fixed: '#b9f474'
  secondary-fixed-dim: '#9ed75b'
  on-secondary-fixed: '#0f2000'
  on-secondary-fixed-variant: '#2e4f00'
  tertiary-fixed: '#ffdad6'
  tertiary-fixed-dim: '#ffb4ac'
  on-tertiary-fixed: '#410002'
  on-tertiary-fixed-variant: '#93000d'
  background: '#fcf9f8'
  on-background: '#1b1c1c'
  surface-variant: '#e5e2e1'
typography:
  h1:
    fontFamily: PingFang SC
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  h2:
    fontFamily: PingFang SC
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  h3:
    fontFamily: PingFang SC
    fontSize: 18px
    fontWeight: '600'
    lineHeight: 26px
  body-lg:
    fontFamily: PingFang SC
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: PingFang SC
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  price-display:
    fontFamily: PingFang SC
    fontSize: 20px
    fontWeight: '500'
    lineHeight: 20px
  label-sm:
    fontFamily: PingFang SC
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 16px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  xs: 4px
  sm: 8px
  md: 12px
  lg: 16px
  xl: 24px
  card-gap: 12px
  section-gap: 24px
  container-margin: 16px
---

## Brand & Style

The design system is centered on a "Boutique Farm-to-Table" philosophy. It aims to evoke the feeling of a premium, curated orchard experience rather than a high-volume marketplace. The personality is warm and personal, reflecting the care of a single merchant who hand-selects every piece of produce.

The visual style follows a **Modern Minimalist** approach with a focus on tactile cleanliness. It prioritizes high-quality white space and refined typography to let the vibrant colors of the fresh produce serve as the primary visual interest. By avoiding heavy borders and complex gradients, the design system creates a calm, trustworthy environment that feels both sophisticated and accessible.

## Colors

The color palette is rooted in nature. The **Natural Green** serves as the primary anchor for action-oriented elements, symbolizing growth and reliability. The **Sprout Green** is used for secondary information like product tags or nutritional highlights, adding a layer of freshness. 

The background and card colors utilize a subtle contrast between off-white and pure white to create soft depth without harsh lines. **Price Red** is reserved strictly for discount scenarios, ensuring that urgency is used sparingly to maintain the premium feel. Text colors are kept in the deep charcoal range rather than pure black to reduce eye strain and feel more "organic."

## Typography

The typography uses **PingFang SC** for its clean, modern aesthetic and excellent legibility in digital interfaces. Titles and headers utilize the **Semibold** weight to establish a clear hierarchy and sense of authority. 

Body text is set in **Regular** for a friendly and readable experience. For pricing, a **Medium** weight is employed to ensure the cost is prominent but not overly aggressive. Line heights are generous throughout the design system to prevent information density and maintain the airy, premium feel of the interface.

## Layout & Spacing

This design system utilizes a **fluid grid** model tailored for mobile environments. It is built on a **4px baseline grid** to ensure mathematical harmony across all components. 

The standard layout features a **16px side margin** for the main container. Elements within sections are separated by a **12px gap** (standard for product cards), while major content sections are separated by a **24px gap** to provide breathing room. This generous use of negative space is essential to maintaining the "boutique" narrative and avoiding the cluttered look of mass-market platforms.

## Elevation & Depth

Hierarchy is established through **tonal layers** and **ambient shadows** rather than heavy borders. The primary depth mechanism is the contrast between the `#FAFAFA` background and the `#FFFFFF` card surfaces.

To provide a sense of "lift" for interactive elements, an **ultra-light shadow** is applied (0 2px 8px rgba(0,0,0,0.04)). This shadow is intentionally subtle to mimic soft, natural light, reinforcing the "fresh and airy" brand personality. Floating action buttons or modal sheets may use a slightly deeper version of this shadow to indicate they sit higher in the stack.

## Shapes

The shape language is "Softly Rounded," striking a balance between modern precision and organic friendliness. 

- **Cards:** Use a **12px radius** to feel substantial yet approachable.
- **Buttons:** All primary buttons are **Capsule-shaped (24px)**, which provides a distinctive, friendly touchpoint that is easy to tap.
- **Inputs:** Use an **8px radius**, offering enough roundness to feel cohesive with the system without losing the functional feel of a form field.
- **Icons:** Linear style with a **2px stroke** and rounded ends to match the overall softness of the UI.

## Components

### Buttons
Primary buttons are full-width or large-scale capsules using the Natural Green `#2E7D32` with white text. Ghost buttons use a 1px stroke of the divider color with Primary Green text.

### Cards
Product cards should be minimal, featuring high-definition imagery on white or light beige backgrounds. Titles are Primary Text, with prices sitting at the bottom left. All cards use the 12px radius and the ultra-light ambient shadow.

### Chips & Tags
Used for categories or product attributes (e.g., "Organic", "Seasonal"). These use the Sprout Green `#8BC34A` background at 10-15% opacity with the full-strength Sprout Green for the text.

### Input Fields
Inputs are outlined with a light grey `#EEEEEE` border, which transitions to Natural Green on focus. Labels are kept in Text Secondary above the field.

### Steppers & Quantities
For adding items to the cart, the design system uses a simple horizontal stepper. The plus and minus buttons are linear icons within subtle circles, ensuring the focus remains on the product quantity.

### Product Imagery
Images are the centerpiece. All photography should be shot in high definition with soft, natural lighting. Backgrounds within photos should be neutral (white or beige) to maintain a seamless transition into the UI.