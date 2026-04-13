# Alembrar

**Alembrar** é um aplicativo Android desenvolvido em Jetpack Compose para gerenciar lembretes da área de transferência de forma organizada e eficiente.

## Funcionalidades
- **Organização por Campos**: Crie grupos para seus lembretes.
- **Busca Avançada**: Pesquise por grupos na tela inicial e por lembretes específicos dentro de cada grupo.
- **Fixação**: Fixe seus campos mais importantes no topo da lista.
- **Notificações**: Configure lembretes periódicos para não esquecer informações importantes.
- **Auto-Update**: O app avisa quando uma nova versão está disponível.

## Desenvolvimento
O projeto utiliza:
- **Jetpack Compose** para a interface.
- **Room Database** para persistência local.
- **Hilt** para injeção de dependência.
- **Ktor** para o sistema de atualizações.

## Como Atualizar o App (para o desenvolvedor)
Sempre que lançar uma nova versão:
1. Gere o APK assinado (vínculo com o GitHub Releases).
2. Atualize o arquivo `version.json` na raiz deste repositório com o novo `versionCode` e `versionName`.
3. O app detectará a mudança automaticamente e oferecerá o download aos usuários.

---
Desenvolvido por **gabrozil**.
