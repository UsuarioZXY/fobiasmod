# Fobias (Minecraft 1.20.1 · Forge 47.3.0)

Mod de terror por niveles, al estilo Backrooms. Cada nivel se abre con un orbe y hay que escapar de él.

## Nivel 0: El Purgatorio

1. Consigue el **Orbe del Purgatorio**: `/nivel orbe` (o pestaña creativa "Herramientas y utilidades").
2. Haz **clic derecho** con el orbe.
3. Tu inventario se guarda y apareces en un **bosque plano, completamente a oscuras**, en modo aventura, con una sola **linterna**.
4. Un **haz de luz blanco (baliza)** marca la salida, a 100-140 bloques. Llega hasta ella para escapar.
5. **El Sonriente** te observa inmóvil unos 25 segundos y luego te persigue. Es invulnerable.
6. Si te atrapa, despiertas de golpe de vuelta en tu mundo. Ganes o pierdas, recuperas tu inventario.

### La linterna
- **Clic derecho**: encender / apagar (cambia la textura del objeto).
- Cuanto **más lejos apuntes**, más fuerte es la luz del punto final y más terreno se ilumina. Un cono de polvo muestra el haz en la oscuridad (alcance 24 bloques).
- Para tener efecto debe estar **en la mano** (principal o secundaria).

### Cómo escapar del Sonriente
| Situación | Qué hace él |
|---|---|
| Linterna encendida y apuntándole | Se frena casi por completo y se tapa la cara |
| Linterna encendida | Te ve a hasta 64 bloques |
| Linterna apagada, caminando | Te oye a menos de 30 bloques |
| Linterna apagada y **agachado** | Solo te nota a menos de 8 bloques |

Sus latidos se aceleran cuanto más cerca está.

### El Sonriente
Alto (2,7 bloques) y flaco, con brazos largos que casi tocan el suelo, dedos larguísimos y una sonrisa enorme con ojos y dientes que brillan en la oscuridad. Tiene animaciones propias:
- **Observando**: inmóvil, cabeza ladeada, siempre girando hacia ti, dedos que se mueven y tics nerviosos.
- **Cazando**: inclinado hacia adelante, zancadas enormes y brazos extendidos con dedos abiertos.
- **Iluminado**: se cubre la cara, tiembla y retrocede a trompicones.
- **Ataque**: se lanza hacia adelante con las manos abiertas.

## Comandos
- `/nivel orbe` — recibir el orbe
- `/nivel linterna` — recibir una linterna
- `/nivel salir` — abandonar el nivel si algo sale mal (devuelve tu inventario)

## Compilar el .jar con GitHub
1. Sube el contenido de esta carpeta a tu repositorio (incluida `.github/workflows`).
2. En **Actions**, el workflow "Compilar mod" corre solo con cada push.
3. Descarga el artefacto **fobias-jar** (contiene `fobias-0.1.0.jar`).

Instálalo en `mods` con Minecraft 1.20.1 y Forge 47.3.0 o superior.

## Ajustes rápidos
- Velocidad, tiempo de espera y detección del Sonriente: constantes en `StalkerEntity.java`.
- Distancia de la salida: constantes en `PurgatoryGame.java`.
- Alcance e intensidad de la linterna: `FlashlightHandler.java`.
- Modelo y animaciones: `StalkerModel.java`. Textura: `assets/fobias/textures/entity/`.
- Aspecto del nivel: `data/fobias/dimension/purgatory.json` y `dimension_type/purgatory.json`.

Antes de publicar, cambia `mod_authors` en `gradle.properties`.
