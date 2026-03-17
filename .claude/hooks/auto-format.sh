#!/bin/bash
# .claude/hooks/auto-format.sh
INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')

if [[ "$FILE_PATH" =~ \.(ts|tsx|js|jsx|css|scss)$ ]]; then
  npx prettier --write "$FILE_PATH" 2>/dev/null
fi
exit 0