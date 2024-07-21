import {nodeResolve} from "@rollup/plugin-node-resolve";
import terser from '@rollup/plugin-terser';

export default {
    input: "src/main/resources/cloud/codestore/client/ui/snippet/code/editor/editor.mjs",
    plugins: [nodeResolve()],
    output: {
        file: "target/editor.js",
        format: "iife",
        plugins: [terser()] // minification
    }
}