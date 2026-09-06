# Placeholders de EdBoost

Requiere PlaceholderAPI instalado y activo. Identificador: `edboost`.

CONVENCIÓN: todos los valores numéricos de boost son MULTIPLICADORES
DIRECTOS, no porcentajes. `1.0` = sin boost. `1.10` = x1.10 (+10%).

| Placeholder                     | Descripción                                                                 |
|----------------------------------|------------------------------------------------------------------------------|
| `%edboost_<economia>%`           | Multiplicador total del jugador en esa economía. Ej: `%edboost_money%` → `1.1` |
| `%edboost_<economia>_percent%`   | El mismo valor como porcentaje con signo. Ej: `+10.0`                        |
| `%edboost_<economia>_max%`       | Multiplicador máximo configurado (`max-boost`) para esa economía.            |
| `%edboost_<economia>_name%`      | Nombre mostrado (`display-name`) configurado para esa economía.              |

`<economia>` debe coincidir con la clave usada en `config.yml` bajo
`economies:`. Para `money`, `tokens`, `gems` (EdTools) la clave debe
coincidir con el nombre que EdTools reporta en
`EdToolsCurrencyAddEvent#getCurrency()`. Para los plugins opcionales
(cada uno solo aplica si ese plugin está instalado) las claves son fijas:

| Plugin              | Claves de economía disponibles                                    |
|----------------------|---------------------------------------------------------------------|
| RivalHarvesterHoes   | `hoes_essence`, `hoes_money`, `hoes_xp`                            |
| RivalPickaxes        | `pickaxes_essence`, `pickaxes_money`, `pickaxes_xp`, `pickaxes_procboost` |
| RivalMobSwords       | `mobswords_essence`, `mobswords_money`, `mobswords_xp`, `mobswords_procboost` |
| CyberLevels          | `cyberlevels_exp`                                                   |

Ej: `%edboost_hoes_essence%`, `%edboost_pickaxes_procboost_percent%`.

## `%edboost_total%` — eliminado

Existía en una versión anterior (cuando el valor almacenado era un
porcentaje aditivo sobre 0.0, y sumar economías tenía sentido). Bajo la
convención de multiplicador directo, cada economía es un multiplicador
independiente de una moneda distinta — sumar `1.10` (money) + `1.05`
(tokens) no representa nada coherente. Si necesitas mostrar varios
boosts juntos, usa `%edboost_<economia>_percent%` por separado para
cada economía.

## Ejemplos

Con esta configuración:

```yaml
economies:
  money:
    display-name: "Dinero"
    max-boost: 2.0
```

- Un jugador con boost x1.10 en `money`:
  - `%edboost_money%` → `1.1`
  - `%edboost_money_percent%` → `+10.0`
  - `%edboost_money_max%` → `2`
  - `%edboost_money_name%` → `Dinero`
- Un jugador sin boost en `money` (nunca se le otorgó nada):
  - `%edboost_money%` → `1` (baseline, sin efecto — no `0`)
  - `%edboost_money_percent%` → `+0.0`

## Notas

- Los valores numéricos crudos (`%edboost_<economia>%` y `_max`) se
  formatean sin ceros ni punto decimal sobrante.
- Un jugador sin boost registrado devuelve `1` (multiplicador neutro),
  nunca `0` — un `0` ahí implicaría vaciar esa economía por completo,
  que no es el comportamiento de "sin boost".
- Si la economía no existe en `config.yml`, `_name` devuelve el propio
  ID tal cual se escribió, y `_max` devuelve un número muy grande (sin
  límite configurado).
