package net.es.oscars.utils.bootstrap;

public class BootClassLoader extends ClassLoader {

        private String moduleName = null;

        public BootClassLoader (String moduleName) {
            super(Thread.currentThread().getContextClassLoader());
            this.moduleName = moduleName;
        }

        public String getModuleName() {
            return this.moduleName;
        }

}

