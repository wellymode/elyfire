# elyfire

A client-side visual fire aspect mod for **Fabric 1.21.11** with Sodium & Reese's Sodium Options integration.

![elyfire icon](src/main/resources/assets/elyfire/icon.png)

## Features

- **Client-Side Fire Aspect Visuals:** Ignites opponents with fire animations upon hitting them with a Fire Aspect weapon, even if the server suppresses vanilla fire packets. Purely visual for you, deals no damage.
- **Last Hit Focus Mode (`lastHitOnly`):** Keep visual fire strictly on your latest attacked opponent (disabled by default).
- **Vanilla Caps:** Fire duration is safely capped at vanilla maximums (4s for Fire Aspect I, 8s for Fire Aspect II).
- **Strict Water Extinguishing:** Opponents immediately extinguish upon entering water.
- **Sodium & Reese's Sodium Options Tab:** Dedicated `elyfire` configuration tab with duration sliders, toggle switches, and a one-click reset to vanilla values button.
- **In-Game Commands:** Quick controls via `/elyfire`, `/elyfire reset`, `/elyfire lasthit`, `/elyfire fire1 <1-4>`, `/elyfire fire2 <1-8>`, and `/elyfire toggle`.

## Contributors

- **milangelvi**
- **Antigravity**

## License

This project is licensed under the [MIT License](LICENSE).
