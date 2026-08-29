# Placeholders de EdBoost

Requiere PlaceholderAPI instalado y activo. Identificador: `edboost`.

| Placeholder                     | Descripción                                                                 |
|----------------------------------|------------------------------------------------------------------------------|
| `%edboost_total%`                | Suma de todos los boosts permanentes del jugador (todas las economías).      |
| `%edboost_<economia>%`           | Valor acumulado del boost del jugador en esa economía. Ej: `%edboost_money%` |
| `%edboost_<economia>_max%`       | Máximo configurado (`max-boost`) para esa economía en `config.yml`.          |
| `%edboost_<economia>_name%`      | Nombre mostrado (`display-name`) configurado para esa economía.              |

`<economia>` debe coincidir con la clave usada en `config.yml` bajo
`economies:` (ej. `money`, `tokens`, `gems`), y con el nombre que EdTools
reporta en `EdToolsCurrencyAddEvent#getCurrency()`.

## Ejemplos

Con esta configuración:

```yaml
economies:
  money:
    display-name: "Dinero"
    max-boost: 1.0
```

- `%edboost_money%` → `0.05` (si el jugador tiene 0.05x acumulado)
- `%edboost_money_max%` → `1`
- `%edboost_money_name%` → `Dinero`
- `%edboost_total%` → suma de `money` + `tokens` + `gems` + ... del jugador

## Notas

- Todos los valores numéricos se formatean sin ceros ni punto decimal
  sobrante (ej. `0.0500` se muestra como `0.05`, `1.0000` como `1`).
- Si el jugador no tiene boost en esa economía, el placeholder devuelve `0`.
- Si la economía no existe en `config.yml`, `%edboost_<economia>_name%`
  devuelve el propio ID tal cual se escribió, y `%edboost_<economia>_max%`
  devuelve un número muy grande (sin límite configurado).
