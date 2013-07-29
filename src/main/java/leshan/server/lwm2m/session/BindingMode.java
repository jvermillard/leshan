package leshan.server.lwm2m.session;

/**
 * Transport binding and Queue Mode
 */
public enum BindingMode {

    /** UDP */
    U,

    /** UDP with Queue Mode */
    UQ,

    /** SMS */
    S,

    /** SMS with Queue Mode */
    SQ,

    /** UDP and SMS */
    US,

    /** UDP with Queue Mode and SMS */
    UQS
}