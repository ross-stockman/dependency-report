#!/bin/zsh

# Ensure the script is run from the directory where it resides
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)

# Directory containing Maven projects
PROJECTS_DIR=$SCRIPT_DIR/workspace

# Raw csv file for the report
REPORT_FILE=$PROJECTS_DIR/dependency-report.csv

# Output report file
REPORT_FILE_XLSX=$PROJECTS_DIR/dependency-report.xlsx

# List of projects to ignore
IGNORE_PROJECTS=("dependency-report")

# Initialize the report file
echo > $REPORT_FILE

# Function to generate a dependency report for a single Maven project
generate_dependency_report() {
  local project_dir=$1
  local project_name=$(basename $project_dir)

  echo "Processing project: $project_name"

  # Run the Maven dependency list command and filter the output to get only dependency items
  dependencies=$(mvn -f $project_dir/pom.xml dependency:list | grep -E '^\[INFO\]    [^:]+:[^:]+:[^:]+:[^:]+(:[^:]+)?$')

  # Iterate over each line of dependencies
  echo "$dependencies" | while read -r line; do
    dependency=$(echo "$line" | awk '{print $2}')
    # Extract the group id, artifact id, and version using awk
    groupId=$(echo "$dependency" | awk -F':' '{print $1}')
    artifactId=$(echo "$dependency" | awk -F':' '{print $2}')
    package=$(echo "$dependency" | awk -F':' '{print $3}')
    version=$(echo "$dependency" | awk -F':' '{print $4}')
    scope=$(echo "$dependency" | awk -F':' '{print $5}')

    # Print or use the variables as needed
    echo "$project_name,$groupId,$artifactId,$version,$package,$scope" >> $REPORT_FILE
  done
}

generate_vulnerability_report() {
  local project_dir=$1
  local project_name=$(basename $project_dir)

  echo "Processing project: $project_name"

  # Run the Maven dependency check report
  mvn -f $project_dir/pom.xml org.owasp:dependency-check-maven:check -Dformat=JSON -q
}

# Iterate through each Maven project directory and generate the report
for project_dir in $PROJECTS_DIR/*; do
  if [ -d "$project_dir" ]; then
    project_name=$(basename $project_dir)
    if [[ ! " ${IGNORE_PROJECTS[@]} " =~ " ${project_name} " ]]; then
      if [ -f "$project_dir/pom.xml" ]; then
        generate_dependency_report $project_dir
#        generate_vulnerability_report $project_dir
      else
        echo "Skipping directory without pom.xml: $project_name"
      fi
    else
      echo "Ignoring project: $project_name"
    fi
  fi
done

echo "Dependency CSV report generated: $REPORT_FILE"

echo "Generating final output report..."

mvn spring-boot:run -Ddependencies="$REPORT_FILE" -Dreport="$REPORT_FILE_XLSX" -q -f $SCRIPT_DIR/pom.xml -P report

echo "Dependency XLSX report generated: $REPORT_FILE_XLSX"