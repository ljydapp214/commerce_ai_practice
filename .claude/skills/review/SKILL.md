---
name: code-review
description: PR 코드 리뷰를 수행합니다
allowed-tools: Read, Grep, Glob, Bash(git diff *)
model: sonnet
---

## 코드 리뷰 절차

1. 변경된 파일 목록을 확인합니다.
2. 각 파일의 diff를 읽고 다음을 검토합니다.
    - 보안 취약점 (OWASP Top 10)
    - 성능 이슈 (N+1 쿼리, 불필요한 렌더링)
    - 코드 스멜 (매직 넘버, 중복 코드)
    - 테스트 커버리지
3. 발견된 이슈를 심각도별로 분류합니다.
4. 개선 제안과 함께 리뷰 결과를 정리합니다.