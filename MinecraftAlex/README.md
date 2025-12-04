# MinecraftAlex — Java Minecraft-like на jMonkeyEngine

MinecraftAlex — это учебный проект по созданию собственного Minecraft-подобного движка на Java с использованием jMonkeyEngine.  
На данный момент реализованы базовые механики:

- генерация мира 20×20 блоков (GRASS / DIRT / STONE);
- освещение и sky-color;
- управление как в Minecraft (WASD + мышь);
- физический персонаж на Bullet Physics;
- корректная коллизия с миром и гравитация.

Цель проекта — постепенно развивать полноценный voxel-движок с чанками, терраформингом, ломанием/постановкой блоков и оптимизацией рендера.

---

## Технологии

- Java 17
- Maven
- jMonkeyEngine 3.3.2-stable
- LWJGL3
- Bullet Physics

---

## Установка и сборка

Требования:

- Java 17+
- Maven 3+

Сборка проекта:

```bash
mvn clean package
