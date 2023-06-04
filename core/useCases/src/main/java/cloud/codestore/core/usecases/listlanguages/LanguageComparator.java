package cloud.codestore.core.usecases.listlanguages;

import cloud.codestore.core.Language;

import java.util.Comparator;

class LanguageComparator implements Comparator<Language> {
    @Override
    public int compare(Language langA, Language langB) {
        //Objective-C should be after C, but before other languages
        if (langA == Language.OBJECTIVE_C) {
            langA = Language.C;
            if (langB == Language.C)
                return 1;
        } else if (langB == Language.OBJECTIVE_C) {
            langB = Language.C;
            if (langA == Language.C)
                return -1;
        }

        return langA.getName().compareToIgnoreCase(langB.getName());
    }
}
