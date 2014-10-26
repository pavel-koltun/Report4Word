package local.koltun.reportfw.docx;

import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;



public class WordprocessingMLPackageSingleton {
    private WordprocessingMLPackageSingleton() {
    }

    private static class SingletonHolder {
        public static WordprocessingMLPackage WORDPROCESSING_ML_PACKAGE;

        static {
            try {
                WORDPROCESSING_ML_PACKAGE = WordprocessingMLPackage.createPackage();
            } catch (InvalidFormatException e) {
                e.printStackTrace();
            }
        }
    }

    public static WordprocessingMLPackage getInstance() {
        return SingletonHolder.WORDPROCESSING_ML_PACKAGE;
    }
}
