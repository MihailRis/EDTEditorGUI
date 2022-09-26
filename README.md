# EDT<sub>3</sub> Editor GUI
The editor is oriented on [EDT3](https://github.com/MihailRis/EDT3/)

## Table of contents
- [Features](#features)
- [Implementation](#implementation)

## Features
- opening EDT<sub>3</sub> files
- drag-and-drop files opening
- editing values of:
  - int and long
  - float and double
  - string
  - bool
- deleting nodes
- creating nodes of all EDT<sub>3</sub> types
- converting groups and lists to bytes node
- converting bytes to group or list back
- text editor for string nodes
- import EDT<sub>2</sub> files
- export to JSON, YAML
- undo and redo actions

## Implementation
Application developed using Swing and Spring Framework: core and context.
Used GTK+ look if available.