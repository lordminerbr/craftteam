# Craft Teams (NeoForge 1.21.1)

Bloqueia o craft de itens de mods por time. As interacoes com os mods continuam livres.

## Como funciona
- Todo slot de resultado de craft (mesa, craft 2x2 do inventario, bancadas que usam ResultSlot)
  e checado no servidor. O item sai ou nao conforme o time do jogador.
- O mod do item e o prefixo do ID (create:cogwheel -> "create").
- Vanilla (minecraft) e sempre liberado por padrao. Edite config/craftteams.json (alwaysAllowed) para mudar.
- Jogador sem time nao craft a nada de mod. Modo criativo ignora o bloqueio.
- Times sao os times normais do Minecraft (/team). A tela abre com /craftteams (operador nivel 2).

## Montar o projeto
1. Baixe o MDK do NeoForge 1.21.1 (NeoForgeMDK/MDK-1.21.1-ModDevGradle no GitHub).
2. No gradle.properties do MDK, ajuste:
   mod_id=craftteams
   mod_name=Craft Teams
   mod_version=1.0.0
   mod_group_id=com.craftteams
   mod_authors=voce
   mod_description=Craft por time
3. Apague os arquivos de exemplo em src/main/java (ExampleMod, Config etc.).
4. Copie a pasta src/ deste projeto por cima da src/ do MDK
   (isso troca o src/main/templates/META-INF/neoforge.mods.toml).
5. Rode ./gradlew build. O jar fica em build/libs/.
6. Coloque o jar na pasta mods do servidor. Quem for abrir a tela precisa dele tambem no cliente.
