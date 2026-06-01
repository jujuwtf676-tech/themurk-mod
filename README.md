# 🖤 The Murk — Mod Minecraft Forge 1.20.1

> *Il observe. Il attend. Ne t'approche pas.*

---

## 📦 Contenu du mod

### L'entité : **The Murk**
Une masse informe de brouillard sombre qui hante les forêts et les zones sombres la nuit.

**Comportement :**
- 🌙 Spawn uniquement la nuit, dans les forêts sombres et le Deep Dark
- ☀️ Brûle au soleil (comme les Endermen)
- 👁️ **Se fige** si le joueur le regarde (angle de vue précis)
- 🚶 Se rapproche silencieusement quand le joueur ne le regarde pas
- ⚡ À moins de 6 blocs : **75% attaque / 25% fuite** (téléportation lointaine)
- 🐄 Les animaux passifs fuient dans un rayon de 12 blocs
- 🕯️ Éteint les torches dans un rayon de 8 blocs
- 🌑 Laisse des particules de **griffures** sur les bûches d'arbres proches

**Effets appliqués au joueur :**

| Effet | Durée | Déclencheur |
|-------|-------|-------------|
| Darkness II | 1 minute | Attaque ou fuite |
| Mining Fatigue III | 1 minute | Attaque ou fuite |
| Nausea | 4 secondes | Attaque ou fuite |
| Slowness I | Continu | Proximité (6-20 blocs) |
| Murk's Gaze | Continu | Être observé à distance |

**Stats :**
- ❤️ 40 PV
- ⚔️ 3 dégâts (faible — le but c'est la peur, pas tuer)
- 🛡️ Résistance aux knockbacks totale
- 👁️ Portée de détection : 64 blocs

### L'item : **Eye of the Murk** (`themurk:murk_eye`)
Drop rare (5% de base) lors de la mort du Murk. Item de lore — peut être utilisé comme trophée ou intégré à un craft personnalisé.

---

## 🔧 Installation et compilation

### Prérequis
- **Java 17** (JDK, pas JRE)
- **Minecraft Forge 1.20.1** (version recommandée : 47.2.0+)
- Git (optionnel)

### Étapes

```bash
# 1. Télécharger le MDK Forge 1.20.1
# https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html
# Extraire le MDK dans un dossier, puis copier les fichiers de CE projet dedans.

# 2. Lancer le setup Forge
./gradlew genEclipseRuns   # pour Eclipse
./gradlew genIntellijRuns  # pour IntelliJ IDEA

# 3. Compiler le mod
./gradlew build

# Le .jar se trouvera dans : build/libs/themurk-1.0.0.jar
```

### Installer le .jar
Copie `themurk-1.0.0.jar` dans le dossier `mods/` de ton installation Minecraft Forge 1.20.1.

---

## 🎨 Textures à créer (OBLIGATOIRE)

Le mod génère le code mais les textures `.png` doivent être créées manuellement.
Taille recommandée : **64×48 px** pour l'entité.

### `assets/themurk/textures/entity/murk.png` *(64×48)*
La texture principale du Murk. Idée visuelle :
- Fond **noir opaque** (`#0a0a0a`)
- Zones semi-transparentes en **gris très sombre** (`#111111` à `#1a1a1a`)
- Quelques pixels de **violet très sombre** (`#1a0033`) pour les contours
- Optionnellement : 2 pixels légèrement plus clairs pour suggérer des "yeux"
- La texture sera rendue semi-transparente par le renderer — donc même une texture sombre simple fonctionnera bien

### `assets/themurk/textures/item/murk_eye.png` *(16×16)*
Un œil sombre et mystérieux :
- Fond noir
- Iris violet foncé (`#3d0066`)
- Pupille noire avec un léger reflet blanc

### Particules *(16×16 chacune)*
- `assets/themurk/textures/particle/murk_smoke_0.png` — nuage sombre, opacité 60%
- `assets/themurk/textures/particle/murk_smoke_1.png` — variante légèrement différente
- `assets/themurk/textures/particle/murk_smoke_2.png` — variante plus petite
- `assets/themurk/textures/particle/murk_scratch_0.png` — 3 traits blancs/gris sur fond transparent (griffures)

> 💡 **Outil recommandé :** [Blockbench](https://www.blockbench.net/) pour les textures d'entité, ou Aseprite/GIMP pour les particules et items.

---

## 🔊 Sons à ajouter (optionnel mais recommandé)

Placer les fichiers `.ogg` dans `assets/themurk/sounds/` :

| Fichier | Description | Style suggéré |
|---------|-------------|---------------|
| `murk_ambient.ogg` | Son ambiant (joué près du joueur) | Murmures graves, craquements |
| `murk_attack.ogg` | Son lors de l'attaque | Grondement grave et brusque |
| `murk_flee.ogg` | Son lors de la fuite | Sifflement aigu et distordu |
| `murk_appear.ogg` | Son d'apparition | Bruit sourd et étouffé |

> 💡 Sons libres de droits recommandés : [Freesound.org](https://freesound.org) (filtrer "horror ambient", "growl", "distorted")

---

## 📁 Structure du projet

```
themurk/
├── build.gradle
├── settings.gradle
├── gradle.properties
└── src/main/
    ├── java/com/themurk/mod/
    │   ├── TheMurkMod.java              ← Point d'entrée du mod
    │   ├── ClientSetup.java             ← Enregistrement renderer (client)
    │   ├── entity/
    │   │   └── MurkEntity.java          ← IA et comportements complets
    │   ├── client/renderer/
    │   │   ├── MurkModel.java           ← Modèle 3D blob informe
    │   │   └── MurkRenderer.java        ← Rendu semi-transparent
    │   ├── registry/
    │   │   ├── ModEntities.java
    │   │   ├── ModEffects.java
    │   │   ├── ModItems.java
    │   │   ├── ModParticles.java
    │   │   └── ModSounds.java
    │   └── event/
    │       └── MurkEventHandler.java    ← Attributs + spawn placement
    └── resources/
        ├── META-INF/mods.toml
        ├── pack.mcmeta
        ├── assets/themurk/
        │   ├── lang/
        │   │   ├── en_us.json
        │   │   └── fr_fr.json
        │   ├── models/item/murk_eye.json
        │   ├── particles/
        │   │   ├── murk_smoke.json
        │   │   └── murk_scratch.json
        │   ├── sounds.json
        │   └── textures/               ← À CRÉER (voir ci-dessus)
        └── data/themurk/
            ├── forge/biome_modifier/
            │   └── murk_spawns.json    ← Zones de spawn
            └── loot_tables/entities/
                └── murk.json           ← Drop Eye of the Murk
```

---

## ⚡ Commandes de test en jeu

```
# Invoquer un Murk sur toi
/summon themurk:murk ~ ~ ~

# Passer à la nuit pour le voir spawner naturellement
/time set midnight

# Donner l'item
/give @p themurk:murk_eye

# Appliquer manuellement l'effet Murk's Gaze
/effect give @p themurk:murks_gaze 60 0
```

---

## 🐛 Dépannage courant

| Problème | Solution |
|----------|----------|
| "Missing texture" pour le Murk | Créer `textures/entity/murk.png` (64×48) |
| Crash au lancement | Vérifier Java 17 et Forge 47.2.0+ |
| Le Murk ne spawn pas | Aller dans une forêt sombre la nuit, attendre ou utiliser `/summon` |
| Particules manquantes | Créer les textures de particules dans `textures/particle/` |

---

*Mod créé avec ❤️ et beaucoup d'obscurité.*
