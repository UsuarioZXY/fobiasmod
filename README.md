# Fobias (Minecraft 1.20.1 · Forge 47.3.0)

Mod sobre fobias: cada fobia que tengas te afecta cuando te enfrentas a lo que temes.

## Fobias actuales

| Fobia | Se activa cuando... | Efectos |
|---|---|---|
| Aracnofobia | Hay arañas a menos de 8 bloques | Lentitud; debilidad (≤5 bloques); náuseas (≤3 bloques) |
| Acrofobia | Hay 8+ bloques de vacío bajo tus pies | Lentitud; náuseas a partir de 16 bloques |
| Claustrofobia | Techo a ≤2 bloques y paredes por 3+ lados | Fatiga de minería y oscuridad |

Los efectos no se aplican en modo creativo ni espectador.

## Comandos

- `/fobia list` — ver tus fobias
- `/fobia add <fobia>` — adquirir una fobia (`aracnofobia`, `acrofobia`, `claustrofobia`)
- `/fobia remove <fobia>` — superarla
- `/fobia clear` — quitarlas todas

## Compilar el .jar con GitHub

1. Crea un repositorio en GitHub y sube todo el contenido de esta carpeta (incluida `.github/`).
2. Ve a la pestaña **Actions**: el workflow "Compilar mod" corre solo con cada push (o dale a *Run workflow*).
3. Al terminar, abre la ejecución y descarga el artefacto **fobias-jar** (contiene `fobias-0.1.0.jar`).
4. Opcional: crea un tag `v0.1.0` y el .jar se publica en **Releases**.

Instálalo en la carpeta `mods` con Minecraft 1.20.1 y Forge 47.3.0 o superior.

## Agregar una fobia nueva

1. Añade un valor en `Phobia.java`.
2. Añade sus textos en `lang/es_es.json` y `lang/en_us.json`.
3. Programa su efecto en `PhobiaEffects.java`.

Antes de publicar, cambia `mod_authors` en `gradle.properties`.
