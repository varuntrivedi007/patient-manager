package com.pm.stack;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.ec2.InstanceClass;
import software.amazon.awscdk.services.ec2.InstanceSize;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.ec2.Vpc;
import software.amazon.awscdk.services.rds.*;
import software.amazon.awscdk.services.route53.CfnHealthCheck;

public class LocalStack extends Stack {

    private final Vpc vpc;

    public LocalStack(final App scope, final String id, final StackProps props){
        super(scope, id, props);

        this.vpc = createVpc();
        DatabaseInstance authServiceDb = createDatabase("AuthServiceDb", "auth-service-db");
        DatabaseInstance patientServiceDb = createDatabase("PatientServiceDb", "patient-service-db");

        CfnHealthCheck authDbHealthCheck =
                createDbHealthCheck(authServiceDb, "AuthServiceDBHealthCheck");

        CfnHealthCheck patientDbHealthCheck =
                createDbHealthCheck(patientServiceDb, "PatientServiceDBHealthCheck");
    }

    private Vpc createVpc(){
        return Vpc.Builder
                .create(this, "PatientManagementVpc")
                .vpcName("PatientManagementVpc") //vpc name
                .maxAzs(2)      //max availability zones
                .build();


    }

    private DatabaseInstance createDatabase(String id, String dbName){
        return DatabaseInstance.Builder
                .create(this, id)
                .engine(DatabaseInstanceEngine.postgres(PostgresInstanceEngineProps.builder()
                        .version(PostgresEngineVersion.VER_17_2).build()))
                .vpc(vpc)
                .instanceType(InstanceType.of(InstanceClass.BURSTABLE2, InstanceSize.MICRO))
                .allocatedStorage(20)
                .credentials(Credentials.fromGeneratedSecret("admin"))
                .databaseName(dbName)
                .removalPolicy(RemovalPolicy.DESTROY) //if you destroy a stack it removes the data and during build it rebuilds everything
                .build();

    }

    private CfnHealthCheck createDbHealthCheck(DatabaseInstance db, String id){
        return CfnHealthCheck.Builder
                .create(this, id)
                .healthCheckConfig(CfnHealthCheck.HealthCheckConfigProperty.builder()
                        .type("TCP")
                        .port(Token.asNumber(db.getDbInstanceEndpointPort())) //port our db is running on
                        .ipAddress(db.getDbInstanceEndpointAddress())
                        .requestInterval(30) //checks every 30 seconds
                        .failureThreshold(3)
                        .build())
                .build();
    }


    public static void main(final String[] args){
        //aws cdk app and we are defining where we want the output when our stack is created
        App app = new App(AppProps.builder().outdir("./cdk.out").build());

        StackProps props = StackProps.builder()
                .synthesizer(new BootstraplessSynthesizer()) //Aws term use to convert our code into cloud formation template
                .build();
        new LocalStack(app, "localStack", props );
        app.synth();
        System.out.println("App synthesizing in progress...");
    }

}
