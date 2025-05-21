# Finstat

A tool to generate consolidated financial report from `pdf` bank statements. Generates Excel `xlsx` file.

## Supported Banks

| Bank                 | Statement File Naming Convension |
|----------------------|----------------------------------|
| HDFC Bank            | hdfc_*.pdf                       |
| State Bank of India  | sbi_*.pdf                        |
| Axis Bank            | axis_*.pdf                       |
| Punjub National Bank | pnb_*.pdf                        |
| United Bank of India | ubi_*.pdf                        |

## Commandline Usage

```
$ ./bin/finstat --help

Usage: finstat [-h] [-c=<classifier>] [-m=<model>] [-r=<report>]
               [-s=<statement>] [-t=<task>] [FILE...]

Generates xlsx report from pdf bank statements.
      
  -c, --classifier=<classifier> The classification algorithm to use. Options: smart, bayes, match
  -h, --help                    Display a help message.
  -m, --category-model=<model>  Custom categories model file path. ex: './categories.json'
  -r, --report=<report>         Report file path. ex; './report.xlsx'
  -s, --statement-model=<statement> Custom bank statments model file path. ex: './statements.json'
  -t, --task=<task>             The task to perform. Options: generate-report or generate-model
  [FILE...]                     One or more file or directory to process. ex: './statements' 'hdfc_1.pdf' 'axis_1.pdf'
```

## Example Usage

Generate excel (`xlsx`) report file

`finstat --task generate-report --report './data/report.xlsx' './data/'`

Generate categories model (`json`) file

`finstat --task generate-model --report './data/report.xlsx' --category-model './data/categories.json'`

## Development

**Build project**

`./gradlew build`

**Run project**

`./gradlew run --args="--task generate-report --report './data/report.xlsx' --category-model './data/categories.json' --statement-model './data/statements.json' './data/accounts'"`

`./gradlew run --args="--task generate-model --report './data/report.xlsx' --category-model './data/categories.json'"`


