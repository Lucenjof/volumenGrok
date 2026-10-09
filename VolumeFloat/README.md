# VolumeFloat

Botón flotante discreto para controlar el volumen de Android **sin que aparezca la barra del sistema**.

Ideal cuando los botones físicos de volumen están rotos.

## Características

- Botón flotante circular semi-transparente y arrastrable
- **No abre el fader de volumen de Android** (usa `flags = 0`)
- Solo sube / baja / mutea el volumen de media
- Muestra una barra de volumen propia y discreta (se oculta sola a los 2.5 s)
- Servicio en primer plano para que no se cierre

## Cómo compilar con GitHub Actions

1. Creá un repositorio nuevo en GitHub
2. Subí **todo el contenido** de esta carpeta al repositorio
3. Andá a la pestaña **Actions** → seleccioná el workflow **Build APK** → **Run workflow**
4. Cuando termine, bajá el artefacto **VolumeFloat-debug** (es el archivo `.apk`)

También se construye automáticamente cada vez que hacés push a `main` o `master`.

## Instalación en el teléfono

1. Descargá el APK del artefacto
2. En el teléfono activá **Instalar apps de orígenes desconocidos**
3. Abrí el APK e instalalo
4. Abrí la app → **Activar botón flotante**
5. Concedé el permiso **Mostrar sobre otras apps**

## Controles

| Acción              | Resultado                          |
|---------------------|------------------------------------|
| Tocar el botón      | Abre la barra de volumen discreta  |
| Arrastrar el botón  | Lo movés por la pantalla           |
| Botón +             | Sube el volumen                    |
| Botón −             | Baja el volumen                    |
| Botón mute          | Mutea / restaura volumen           |

## Requisitos

- Android 8.0 (API 26) o superior
- Permiso de overlay (mostrar sobre otras apps)
