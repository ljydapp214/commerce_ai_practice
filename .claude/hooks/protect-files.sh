#!/bin/bash
# .claude/hooks/protect-files.sh
INPUT=$(cat)
FILE_PATH=$(echo "$INPUT" | jq -r '.tool_input.file_path // empty')

PROTECTED_PATTERNS=(".env" "package-lock.json" "yarn.lock" ".git/")
for pattern in "${PROTECTED_PATTERNS[@]}"; do
   if [[ "$FILE_PATH" == *"$pattern"* ]]; then
       echo "BLOCKED: $FILE_PATH is protected. This file should not be modified by AI." >&2
       exit 2
   fi
done
exit 0