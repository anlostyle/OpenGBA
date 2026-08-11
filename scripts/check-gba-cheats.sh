#!/usr/bin/env bash
set -euo pipefail

root_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
cheat_dir="$root_dir/lemuroid-app/src/main/assets/gba-cheats"

file_count=$(find "$cheat_dir" -maxdepth 1 -type f -name '*.cht' | wc -l | tr -d ' ')
test "$file_count" -eq 512

read -r games descriptions codes < <(
    awk -F' = ' '
        /^cheats =/ { games++ }
        /^cheat[0-9]+_desc =/ { descriptions++ }
        /^cheat[0-9]+_code =/ { codes++ }
        END { print games, descriptions, codes }
    ' "$cheat_dir"/*.cht
)

test "$games" -eq 512
test "$descriptions" -eq "$codes"
test -s "$cheat_dir/LICENSE.txt"
test -s "$cheat_dir/README.txt"

awk -F' *= *' '
    function reset(number) {
        for (number in descriptions) delete descriptions[number]
        for (number in codes) delete codes[number]
        declared = description_count = code_count = 0
    }
    function verify(path, number, valid) {
        valid = declared > 0 && description_count == declared && code_count == declared
        for (number = 0; number < declared; number++) {
            valid = valid && descriptions[number] == 1 && codes[number] == 1
        }
        if (!valid) {
            print "Invalid cheat indexes: " path > "/dev/stderr"
            failed = 1
        }
    }
    FNR == 1 {
        if (NR > 1) verify(previous_file)
        reset()
        previous_file = FILENAME
    }
    /^cheats *=/ { declared = $2 + 0 }
    /^cheat[0-9]+_desc *=/ {
        number = $1
        sub(/^cheat/, "", number)
        sub(/_desc$/, "", number)
        descriptions[number]++
        description_count++
    }
    /^cheat[0-9]+_code *=/ {
        number = $1
        sub(/^cheat/, "", number)
        sub(/_code$/, "", number)
        codes[number]++
        code_count++
    }
    END {
        verify(previous_file)
        exit failed
    }
' "$cheat_dir"/*.cht

echo "GBA cheats OK: $games games, $codes named codes"
