import {
    crosshairCursor,
    EditorView,
    highlightActiveLine,
    highlightActiveLineGutter,
    highlightSpecialChars,
    keymap,
    lineNumbers,
    placeholder,
    rectangularSelection
} from "@codemirror/view"
import {Compartment, EditorState} from "@codemirror/state"
import {
    bracketMatching,
    defaultHighlightStyle,
    indentOnInput,
    indentUnit,
    syntaxHighlighting
} from "@codemirror/language";
import {defaultKeymap, history, historyKeymap, indentWithTab} from "@codemirror/commands";
import {closeBrackets} from "@codemirror/autocomplete";
import {highlightSelectionMatches} from "@codemirror/search";

import {java} from "@codemirror/lang-java";
import {css} from "@codemirror/lang-css";
import {cpp} from "@codemirror/lang-cpp";
import {go} from "@codemirror/lang-go";
import {html} from "@codemirror/lang-html";
import {javascript} from "@codemirror/lang-javascript";
import {php} from "@codemirror/lang-php";
import {python} from "@codemirror/lang-python";
import {xml} from "@codemirror/lang-xml";
import {sql} from "@codemirror/lang-sql";
import {yaml} from "@codemirror/lang-yaml";

const language = new Compartment();
const readonly = new Compartment();

const state = EditorState.create({
    extensions: [
        lineNumbers(),
        highlightActiveLineGutter(),
        highlightSpecialChars(),
        history(),
        indentOnInput(),
        syntaxHighlighting(defaultHighlightStyle, {fallback: true}),
        bracketMatching(),
        closeBrackets(),
        rectangularSelection(),
        crosshairCursor(),
        highlightActiveLine(),
        highlightSelectionMatches(),
        keymap.of([
            ...defaultKeymap,
            ...historyKeymap,
            ...indentWithTab
        ]),
        language.of([]),
        readonly.of(EditorState.readOnly.of(true)),
        indentUnit.of("    "),
        placeholder("Code")
    ]
})

const view = new EditorView({
    state,
    parent: document.body
});

window.editor = {
    setEditable: function(editable) {
        view.dispatch({
            effects: readonly.reconfigure(EditorState.readOnly.of(!editable))
        });
    },

    setLanguage: function(languageId) {
        const lang = function() {
            switch(languageId) {
                // case "0":
                //     return text();
                // case "1":
                //     return c();
                // case "2":
                //     return objectivec();
                case "3":
                    return cpp();
                // case "4":
                //     return csharp();
                // case "5":
                //     return cobol();
                // case "6":
                //     return fortran();
                case "7":
                    return html();
                case "8":
                    return css();
                case "9":
                    return javascript();
                case "10":
                    return java();
                case "11":
                    return sql();
                // case "12":
                //     return scala();
                // case "13":
                //     return lua();
                // case "14":
                //     return latex();
                // case "15":
                //     return lisp();
                // case "16":
                //     return groovy();
                // case "17":
                //     return perl();
                case "18":
                    return python();
                case "19":
                    return php();
                // case "20":
                //     return ruby();
                case "21":
                    return yaml();
                case "22":
                    return xml();
                // case "23":
                //     return matlab();
                // case "24":
                //     return mathematica();
                // case "25":
                //     return swift();
                case "26":
                    return go();
                // case "27":
                //     return shell();
                // case "28":
                //     return dockerfile();
                // case "29":
                //     return batch();
                case "30":
                    return javascript({typescript: true});
                // case "31":
                //     return kotlin();
                default:
                    return [];
            }
        }();

        view.dispatch({
            effects: language.reconfigure(lang)
        });
    },

    setContent: function(content) {
        view.dispatch({
            changes: {
                from: 0,
                to: view.state.doc.length,
                insert: content
            }
        });
    },

    getContent: function() {
        return view.state.doc.toString();
    },
};