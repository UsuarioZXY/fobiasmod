# Fobias (Minecraft 1.20.1 · Forge 47.3.0)

Mod de terror basado en fobias. Cada fobia tendrá su propio mundo al que se entra con un orbe, y del que hay que escapar.

## Mundo 1: el bosque oscuro (nictofobia)

1. Consigue el **Orbe de la Oscuridad**: `/fobia orbe` (o pestaña creativa "Herramientas y utilidades").
2. Haz **clic derecho** con el orbe.
3. Tu inventario se guarda y apareces en un **bosque plano de noche** con solo una **linterna**.
4. Una **luz blanca (baliza)** marca la salida, a 100-140 bloques. Llega hasta ella para escapar.
5. El **Acechador** te observa unos 25 segundos y luego te persigue. Es invulnerable.
   - Si **apuntas la linterna hacia él** (con la linterna en la mano), se frena casi por completo.
   - Si te alcanza, despiertas de golpe de vuelta en tu mundo.
6. Al ganar o perder recuperas tu inventario y vuelves a donde estabas. Si tenías nictofobia (`/fobia add nictofobia`), al escapar la superas.

Notas:
- Necesita dificultad **Fácil o superior** (en Pacífico el Acechador no ataca).
- En el bosque oscuro estás en modo aventura y no aparece ningún otro mob.
- Si algo falla: `/fobia salir` te saca y te devuelve el inventario.

## Fobias pasivas (fuera de los mundos)

| Fobia | Se activa cuando... | Efectos |
|---|---|---|
| Aracnofobia | Hay arañas a menos de 8 bloques | Lentitud; debilidad (≤5); náuseas (≤3) |
| Claustrofobia | Techo a ≤2 bloques y paredes por 3+ lados | Fatiga de minería y oscuridad |
| Nictofobia | Nivel de luz muy bajo | Debilidad y lentitud |

No se aplican en creativo ni espectador.

## Comandos

- `/fobia list`, `/fobia add <fobia>`, `/fobia remove <fobia>`, `/fobia clear`
- `/fobia orbe` — recibir el orbe de la oscuridad
- `/fobia salir` — abandonar el mundo en el que estás

## Compilar el .jar con GitHub

1. Sube el contenido de esta carpeta a tu repositorio (incluida `.github/workflows`).
2. En **Actions**, el workflow "Compilar mod" corre solo con cada push.
3. Descarga el artefacto **fobias-jar** (contiene `fobias-0.1.0.jar`).

Instálalo en `mods` con Minecraft 1.20.1 y Forge 47.3.0 o superior.

## Ajustes rápidos

- Velocidad y tiempo de espera del Acechador: constantes al inicio de `StalkerEntity.java`.
- Distancia de la salida, tamaño y alcance: constantes en `DarkForestGame.java`.
- Intensidad y alcance de la linterna: constantes en `FlashlightHandler.java`.
- La dimensión (aspecto del terreno) está en `data/fobias/dimension/dark_forest.json`.

Antes de publicar, cambia `mod_authors` en `gradle.properties`.
