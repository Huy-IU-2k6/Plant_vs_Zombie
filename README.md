# Plants vs Zombies (PVZ) Clone — Java + LibGDX

A simplified **Plants vs Zombies–style** game clone built for an **Object-Oriented Programming** course project using **Java** and **LibGDX**.

> Disclaimer: This is a fan-made educational project. Plants vs. Zombies is owned by PopCap / EA.

## Demo (Click to watch)

[![PVZ Demo](doc/screenshots/image2.png)](https://www.veed.io/view/7398bc50-0a78-4df9-8a98-2249592c53cf?source=editor&panel=share)

- Gameplay screenshots: **/doc/screenshots/**
- Demo video: **/doc/video/demo.mp4**
- UML diagrams: **/doc/uml/** *(export from Lucidchart/PlantUML)*

---

## Features

- Grid-based plant placement (rows/cols)
- Multiple plant types (attack / producer / bomb-style)
- Multiple zombie types with different stats/behaviors
- Projectile system (e.g., normal pea / frozen pea)
- Basic game loop: waves → defense → win/lose → game over screen
- Modular “systems” style update loop (render, movement, collision, cleanup, etc.)

---

## Tech Stack

- **Java**
- **LibGDX**
- Gradle build (LibGDX multi-module style)

---

## Project Structure (high level)

```
pvz.com/
├─ screens/ # GameScreen, GameOverScreen, screen flow
├─ logic/ # GameWorld, GameState, controllers (HUD, placement, waves...)
├─ entities/ # Entity, Plant, Zombies, Sun, Projectile...
│ └─ components/ # Position, Bounds, Health, Animation, Damage, Cooldown...
├─ systems/ # Render, Movement, Collision, Cleanup, Attack, Sun systems...
└─ managers/ # Config, scale/design helpers, background, assets...
```

---

## How to Run (Desktop)

### Requirements
- **JDK 8+** (11/17 recommended)
- Gradle (or use the Gradle wrapper included in the repo)

### Run
```bash
./gradlew desktop:run
````

### Build

```bash
./gradlew build
```

> If you get “File not found” for textures, double-check your asset paths under `assets/` (LibGDX uses internal asset paths like `images/...`).

---

## Controls (example)

* **Left click**: select plant card / place plant on grid
* **ESC**: pause/back (if implemented)

> Update this section to match your current input logic.

---

## Configuration

Common tuning values are typically in:

* `DesignConfig` (rows/cols, lane count, layout constants)
* Wave timing/spawn parameters in `ZombieWaveController`
* Attack cooldown / projectile offsets in `PlantAttackSystem`

---

## Documentation

* Report / presentation assets live in:

  * `doc/uml/` (class/package diagrams)
  * `doc/screenshots/` (game screenshots)
  * `doc/slides/` (presentation slides export)

Recommended diagrams:

* **Package overview UML**
* **Game loop flow** (GameScreen → GameWorld → Systems → GameOverScreen)
* **Key class diagrams** (Plant/Zombie inheritance + components)

---

## Team & Responsibilities

* **UI Designer**: HUD layout, plant cards, placement UI flow, game over overlay
* **Zombie Developer**: zombie classes, stats/animations, wave spawning & behaviors
* **Plant Developer**: plant classes, attacks/projectiles, costs/cooldowns, plant states

*(Replace names/IDs below)*

| Member           | Role        | Student ID        |
| ---------------- | ----------- | ----------------- |
| Lê Thái Đức Tùng | UI Designer | ITDSIU24055       |
| Nguyễn Duy Phước | Zombies     | ITITWE23026       |
| Phạm Gia Huy     | Plants      | ITCSIU24034       |

---

## Contributing (Team Workflow)

* Branch naming: `feature/<name>` or `fix/<name>`
* Keep assets organized under `assets/images/...`
* Prefer small PRs and clear commit messages

---

## Roadmap (Optional)

* [ ] Add more zombie/plant varieties
* [ ] Balance tuning (sun economy, wave pacing)
* [ ] Sound effects + music toggle
* [ ] Save/load or level selection

---
