import {nodeResolve} from "@rollup/plugin-node-resolve";
import terser from '@rollup/plugin-terser';

const PATH = "src/main/resources/cloud/codestore/client/ui/snippet/code/editor/";

export default {
    input: PATH + "editor.mjs",
    plugins: [nodeResolve()],
    output: {
        file: PATH + "editor.js",
        format: "iife",
        plugins: [terser()] // minification
    }
}