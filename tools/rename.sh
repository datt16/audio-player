#!/bin/bash

# Check if a new name is provided
if [ -z "$1" ]; then
    echo "使用方法: $0 <新しいプロジェクト名>"
    echo "例: $0 ChatIt"
    exit 1
fi

# Set locale for sed
export LC_ALL=C
export LANG=C

NEW_NAME=$1
OLD_NAME="Abstraction"
OLD_PACKAGE="io.github.datt16.abstraction"
NEW_PACKAGE="io.github.datt16.$(echo "$1" | tr '[:upper:]' '[:lower:]')"

echo "プロジェクト名を $OLD_NAME から $NEW_NAME に変更します"
echo "パッケージ名を $OLD_PACKAGE から $NEW_PACKAGE に変更します"

# Confirm with the user
read -p "続行しますか？ (y/N): " confirm
if [ "$confirm" != "y" ] && [ "$confirm" != "Y" ]; then
    echo "操作をキャンセルしました"
    exit 1
fi

# Replace strings in files
echo "ファイル内の文字列を置換しています..."
# Replace package names (lowercase)
find . -type f -not -path "*/\.*" -not -path "*/build/*" -not -path "*/tools/*" -exec sed -i '' "s/${OLD_PACKAGE}/${NEW_PACKAGE}/g" {} +

# Complex replacements for various word combinations
echo "複合ワードを置換しています..."
find . -type f -not -path "*/\.*" -not -path "*/build/*" -not -path "*/tools/*" -exec sed -i '' "
    # Replace when Abstraction is at the start of a camelCase or PascalCase word
    s/${OLD_NAME}\([A-Z][a-zA-Z0-9]*\)/${NEW_NAME}\1/g
    # Replace when Abstraction is after a dot (e.g., .Abstraction)
    s/\\.${OLD_NAME}/\\.${NEW_NAME}/g
    # Replace when Abstraction is standalone
    s/\\b${OLD_NAME}\\b/${NEW_NAME}/g
    # Replace Theme.Abstraction pattern specifically
    s/Theme\\.${OLD_NAME}/Theme.${NEW_NAME}/g
    # Replace package-style references
    s/package\\s\\+${OLD_NAME}/package ${NEW_NAME}/g
    # Replace import-style references
    s/import\\s\\+${OLD_NAME}/import ${NEW_NAME}/g
" {} +

# Update specific files
echo "設定ファイルを更新しています..."
# Update settings.gradle.kts
if [ -f "settings.gradle.kts" ]; then
    sed -i '' "s/rootProject.name = \"${OLD_NAME}\"/rootProject.name = \"${NEW_NAME}\"/g" settings.gradle.kts
fi

# Update .idea/.name if it exists
if [ -f ".idea/.name" ]; then
    echo "${NEW_NAME}" > .idea/.name
fi

# Update build.gradle.kts
if [ -f "app/build.gradle.kts" ]; then
    sed -i '' "s/namespace = \"${OLD_PACKAGE}\"/namespace = \"${NEW_PACKAGE}\"/g" app/build.gradle.kts
    sed -i '' "s/applicationId = \"${OLD_PACKAGE}\"/applicationId = \"${NEW_PACKAGE}\"/g" app/build.gradle.kts
fi

# Rename files with complex patterns
echo "ファイル名を変更しています..."
find . -type f -not -path "*/\.*" -not -path "*/build/*" -not -path "*/tools/*" | while read -r file; do
    # Get directory and filename separately
    dir=$(dirname "$file")
    filename=$(basename "$file")

    # Apply different replacement patterns to filename
    new_filename=$(echo "$filename" | sed -E "
        # Replace when Abstraction is at the start of a camelCase or PascalCase word
        s/${OLD_NAME}([A-Z][a-zA-Z0-9]*)/${NEW_NAME}\1/g
        # Replace when Abstraction is standalone
        s/\\b${OLD_NAME}\\b/${NEW_NAME}/g
        # Replace when Abstraction is part of a compound word
        s/([a-z])${OLD_NAME}/\1${NEW_NAME}/g
    ")

    # Only rename if the filename actually changed
    if [ "$filename" != "$new_filename" ]; then
        mv "$file" "$dir/$new_filename"
        echo "ファイル名を変更: $filename → $new_filename"
    fi
done

# Move and reorganize package directory
echo "パッケージディレクトリを移動しています..."
old_dir="app/src/main/java/io/github/datt16/abstraction"
new_dir="app/src/main/java/io/github/datt16/$(echo "$1" | tr '[:upper:]' '[:lower:]')"

if [ -d "$old_dir" ]; then
    # Create new directory
    mkdir -p "$new_dir"

    # Find all directories in the old location and recreate them in the new location
    find "$old_dir" -type d | while read -r dir; do
        # Skip the root directory itself
        if [ "$dir" = "$old_dir" ]; then
            continue
        fi
        # Get the relative path from old_dir
        rel_path="${dir#$old_dir/}"
        # Create the same directory structure in new location
        if [ ! -z "$rel_path" ]; then
            mkdir -p "$new_dir/$rel_path"
        fi
    done

    # Move all files maintaining the directory structure
    find "$old_dir" -type f | while read -r file; do
        # Get the relative path from old_dir
        rel_path="${file#$old_dir/}"
        # Move the file to the same relative path in new location
        mv "$file" "$new_dir/$rel_path"
    done

    # Remove the old directory after all files have been moved
    rm -rf "$old_dir"
fi

# Update theme references in XML files
echo "リソースファイルを更新しています..."
find . -name "themes.xml" -exec sed -i '' "s/Theme.${OLD_NAME}/Theme.${NEW_NAME}/g" {} +
find . -name "strings.xml" -exec sed -i '' "s/name=\"app_name\">${OLD_NAME}/name=\"app_name\">${NEW_NAME}/g" {} +

echo "完了しました！"
echo "以下の作業を行ってください："
echo "1. Android Studioでプロジェクトを再度開く"
echo "2. プロジェクトを再ビルドする"
echo "3. 必要に応じてR.javaを再生成する"
