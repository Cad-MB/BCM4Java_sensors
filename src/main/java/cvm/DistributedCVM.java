package cvm;

import fr.sorbonne_u.components.cvm.AbstractDistributedCVM;

public class DistributedCVM
    extends AbstractDistributedCVM {

    String[] jvmUris;

    public DistributedCVM(String[] args) throws Exception {
        super(args);
        this.jvmUris = this.configurationParameters.getJvmURIs();
    }

    @Override
    public void instantiateAndPublish() throws Exception {
        System.out.println(getThisJVMURI());
        super.instantiateAndPublish();
    }

    @Override
    public void interconnect() throws Exception {
        super.interconnect();
    }

    public static void main(String[] args) throws Exception {
        DistributedCVM cvm = new DistributedCVM(new String[]{
            "test-uri",
            "/home/s4id/Projects/BCM4Java_sensors/src/main/resources/configs/rmi/config.xml"
        });
        cvm.startStandardLifeCycle(2000);
        Thread.sleep(2000);
        System.exit(0);
    }

}
