---
name: Fresh Fruit Diary Merchant Portal
colors:
  surface: '#fbf9f9'
  surface-dim: '#dbdad9'
  surface-bright: '#fbf9f9'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f5f3f3'
  surface-container: '#efeded'
  surface-container-high: '#e9e8e7'
  surface-container-highest: '#e3e2e2'
  on-surface: '#1b1c1c'
  on-surface-variant: '#40493d'
  inverse-surface: '#303031'
  inverse-on-surface: '#f2f0f0'
  outline: '#707a6c'
  outline-variant: '#bfcaba'
  surface-tint: '#1b6d24'
  primary: '#0d631b'
  on-primary: '#ffffff'
  primary-container: '#2e7d32'
  on-primary-container: '#cbffc2'
  inverse-primary: '#88d982'
  secondary: '#4a6175'
  on-secondary: '#ffffff'
  secondary-container: '#cde5fe'
  on-secondary-container: '#50677b'
  tertiary: '#8d3f00'
  on-tertiary: '#ffffff'
  tertiary-container: '#b35200'
  on-tertiary-container: '#ffeee6'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#a3f69c'
  primary-fixed-dim: '#88d982'
  on-primary-fixed: '#002204'
  on-primary-fixed-variant: '#005312'
  secondary-fixed: '#cde5fe'
  secondary-fixed-dim: '#b1c9e1'
  on-secondary-fixed: '#031d2f'
  on-secondary-fixed-variant: '#32495d'
  tertiary-fixed: '#ffdbc9'
  tertiary-fixed-dim: '#ffb68d'
  on-tertiary-fixed: '#321200'
  on-tertiary-fixed-variant: '#763300'
  background: '#fbf9f9'
  on-background: '#1b1c1c'
  surface-variant: '#e3e2e2'
typography:
  h1:
    fontFamily: Be Vietnam Pro
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
  h2:
    fontFamily: Be Vietnam Pro
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
  h3:
    fontFamily: Be Vietnam Pro
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Be Vietnam Pro
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  body-sm:
    fontFamily: Be Vietnam Pro
    fontSize: 12px
    fontWeight: '400'
    lineHeight: 18px
  label-bold:
    fontFamily: Be Vietnam Pro
    fontSize: 14px
    fontWeight: '600'
    lineHeight: 20px
  table-header:
    fontFamily: Be Vietnam Pro
    fontSize: 13px
    fontWeight: '600'
    lineHeight: 16px
    letterSpacing: 0.02em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  unit: 4px
  container-padding-pc: 24px
  container-padding-mobile: 16px
  gutter: 16px
  table-row-height: 48px
  stack-xs: 4px
  stack-sm: 8px
  stack-md: 16px
  stack-lg: 24px
---

## Brand & Style
The design system for Fresh Fruit Diary centers on the intersection of organic vitality and operational precision. The target audience consists of fruit merchants and logistics managers who require a high-efficiency tool to manage perishable inventory and time-sensitive orders. 

The visual style is **Corporate / Modern** with a focus on high-density information display. It utilizes a clean, structured interface that prioritizes legibility and rapid data scanning. While the brand color evokes nature and freshness, the UI remains strictly professional, avoiding decorative elements in favor of functional clarity and a systematic layout that instills a sense of reliability and control.

## Colors
This design system uses a palette rooted in **Nature Green (#2E7D32)** to reinforce the "fresh" brand identity. The color architecture is designed to support complex status tracking and data visualization.

- **Primary & Action:** Nature Green is used for primary actions, success states, and the "Delivering" status.
- **Surface & Neutrals:** A light gray background (#F5F5F5) is used to reduce eye strain, with white surfaces for containers.
- **Status System:** Highly differentiated colors are assigned to order lifecycle stages to ensure merchants can identify urgent tasks (e.g., Pending and Refund) at a glance. 
- **Data Visualization:** A secondary palette of Warm Orange, Gray Blue, and Yellow provides high contrast for charts and analytics widgets without clashing with the primary brand green.

## Typography
The typography system prioritizes utilitarian readability across both Latin and Chinese characters. **Be Vietnam Pro** is used for its contemporary, professional feel and excellent legibility in high-density environments. **PingFang SC** is utilized for Chinese language support, ensuring a seamless weight match with the primary typeface.

In this design system, the scale is optimized for management tools: **14px** serves as the standard body size to balance information density and legibility. **12px** is reserved for metadata and secondary labels within tables. Bold weights are used sparingly to highlight critical data points and section headers.

## Layout & Spacing
The layout follows a **Fluid Grid** philosophy to accommodate the 1440x900 PC standard while scaling gracefully down to mobile devices. 

- **PC Layout:** Uses a 12-column grid with a fixed 240px left-hand sidebar navigation. Dashboard widgets utilize a 16px gutter.
- **Mobile Layout:** Transitions to a single-column stacked layout with a bottom navigation bar or a collapsible "hamburger" menu for deep navigation.
- **High Density:** Vertical spacing is tightened. Table rows are set to a 48px height to maximize the number of visible records without sacrificing touch/click targets. 
- **Rhythm:** All margins and paddings are multiples of a 4px base unit to maintain a rigorous visual rhythm.

## Elevation & Depth
This design system employs **Tonal Layers** combined with **Low-contrast Outlines** to create hierarchy. 

The primary background uses a neutral off-white or light gray. Surfaces (cards, table containers) are pure white with a 1px border (#E0E0E0). Shadows are used exclusively for floating elements like dropdowns, modals, and tooltips; these shadows are soft, neutral, and have a wide blur radius to prevent the UI from feeling "heavy." This approach ensures that the high density of data doesn't lead to visual clutter, keeping the focus on the information rather than the container.

## Shapes
To maintain a professional, management-tool aesthetic, the design system utilizes **Soft (Level 1)** roundedness. 

Standard components like input fields, buttons, and cards feature a **4px (0.25rem)** corner radius. This subtle rounding provides a modern touch that aligns with the brand's organic nature without the "playfulness" of pill-shaped elements. Large containers like dashboard widgets may use **8px (0.5rem)** to distinguish them from smaller UI components.

## Components
Consistent component behavior is vital for the efficiency of the merchant portal:

- **High-Density Tables:** Features fixed headers, zebra-striping on hover, and condensed padding. Action buttons within rows should be icon-only or secondary style to save horizontal space.
- **Status Badges:** Use a "Subtle Tag" style (light background tint with high-contrast text) using the status colors defined in the palette.
- **Sidebar Navigation:** A dark or high-contrast sidebar utilizing the primary color or a deep neutral to anchor the layout. Active states should be clearly marked with a vertical "Nature Green" bar.
- **Dashboard Widgets:** Individual cards containing data visualizations. Each widget should have a standard header with "Filter" or "Expand" actions.
- **Forms:** Labels are positioned above the input fields for better scanning on mobile. Input fields use the 4px roundedness and a 1px border that turns Green (#2E7D32) on focus.
- **Buttons:** Primary buttons are solid Nature Green with white text. Secondary buttons use an outline style.