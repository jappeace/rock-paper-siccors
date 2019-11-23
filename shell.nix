{ pkgs ? import ./pin.nix }:
pkgs.mkShell{
    buildInputs = [
        pkgs.leiningen
        pkgs.ngrok
    ];
}

