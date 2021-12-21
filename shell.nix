{ pkgs ? import <nixpkgs> {} }:
    with pkgs; mkShell {
        nativeBuildInputs = [ sbt dotty jdk11 graphviz ];
    }