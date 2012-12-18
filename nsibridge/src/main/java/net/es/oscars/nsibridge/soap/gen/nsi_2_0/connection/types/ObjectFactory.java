
package net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ModifyCancelConfirmed_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "modifyCancelConfirmed");
    private final static QName _Modify_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "modify");
    private final static QName _ReserveFailed_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "reserveFailed");
    private final static QName _ProvisionConfirmed_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "provisionConfirmed");
    private final static QName _Provision_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "provision");
    private final static QName _Notification_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "notification");
    private final static QName _ModifyCancelFailed_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "modifyCancelFailed");
    private final static QName _ModifyCancel_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "modifyCancel");
    private final static QName _ReleaseFailed_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "releaseFailed");
    private final static QName _TerminateConfirmed_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "terminateConfirmed");
    private final static QName _QueryFailed_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "queryFailed");
    private final static QName _ReleaseConfirmed_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "releaseConfirmed");
    private final static QName _ModifyCheck_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "modifyCheck");
    private final static QName _Terminate_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "terminate");
    private final static QName _Query_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "query");
    private final static QName _Reserve_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "reserve");
    private final static QName _ModifyFailed_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "modifyFailed");
    private final static QName _ModifyCheckFailed_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "modifyCheckFailed");
    private final static QName _Release_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "release");
    private final static QName _ModifyCheckConfirmed_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "modifyCheckConfirmed");
    private final static QName _ReserveConfirmed_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "reserveConfirmed");
    private final static QName _QueryConfirmed_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "queryConfirmed");
    private final static QName _ModifyConfirmed_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "modifyConfirmed");
    private final static QName _ProvisionFailed_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "provisionFailed");
    private final static QName _TerminateFailed_QNAME = new QName("http://schemas.ogf.org/nsi/2012/03/connection/types", "terminateFailed");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: net.es.oscars.nsibridge.soap.gen.nsi_2_0.connection.types
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link DetailedPathType }
     * 
     */
    public DetailedPathType createDetailedPathType() {
        return new DetailedPathType();
    }

    /**
     * Create an instance of {@link ModifyCheckType }
     * 
     */
    public ModifyCheckType createModifyCheckType() {
        return new ModifyCheckType();
    }

    /**
     * Create an instance of {@link NotificationRequestType }
     * 
     */
    public NotificationRequestType createNotificationRequestType() {
        return new NotificationRequestType();
    }

    /**
     * Create an instance of {@link GenericConfirmedType }
     * 
     */
    public GenericConfirmedType createGenericConfirmedType() {
        return new GenericConfirmedType();
    }

    /**
     * Create an instance of {@link ReserveType }
     * 
     */
    public ReserveType createReserveType() {
        return new ReserveType();
    }

    /**
     * Create an instance of {@link QueryDetailsResultType }
     * 
     */
    public QueryDetailsResultType createQueryDetailsResultType() {
        return new QueryDetailsResultType();
    }

    /**
     * Create an instance of {@link ReservationStateType }
     * 
     */
    public ReservationStateType createReservationStateType() {
        return new ReservationStateType();
    }

    /**
     * Create an instance of {@link ActivationStateType }
     * 
     */
    public ActivationStateType createActivationStateType() {
        return new ActivationStateType();
    }

    /**
     * Create an instance of {@link ReservationConfirmCriteriaType }
     * 
     */
    public ReservationConfirmCriteriaType createReservationConfirmCriteriaType() {
        return new ReservationConfirmCriteriaType();
    }

    /**
     * Create an instance of {@link PathType }
     * 
     */
    public PathType createPathType() {
        return new PathType();
    }

    /**
     * Create an instance of {@link StpListType }
     * 
     */
    public StpListType createStpListType() {
        return new StpListType();
    }

    /**
     * Create an instance of {@link GenericRequestType }
     * 
     */
    public GenericRequestType createGenericRequestType() {
        return new GenericRequestType();
    }

    /**
     * Create an instance of {@link QuerySummaryResultType }
     * 
     */
    public QuerySummaryResultType createQuerySummaryResultType() {
        return new QuerySummaryResultType();
    }

    /**
     * Create an instance of {@link QueryFilterType }
     * 
     */
    public QueryFilterType createQueryFilterType() {
        return new QueryFilterType();
    }

    /**
     * Create an instance of {@link QueryFailedType }
     * 
     */
    public QueryFailedType createQueryFailedType() {
        return new QueryFailedType();
    }

    /**
     * Create an instance of {@link QueryConfirmedType }
     * 
     */
    public QueryConfirmedType createQueryConfirmedType() {
        return new QueryConfirmedType();
    }

    /**
     * Create an instance of {@link ChildDetailedListType }
     * 
     */
    public ChildDetailedListType createChildDetailedListType() {
        return new ChildDetailedListType();
    }

    /**
     * Create an instance of {@link ModifyCheckConfirmedType }
     * 
     */
    public ModifyCheckConfirmedType createModifyCheckConfirmedType() {
        return new ModifyCheckConfirmedType();
    }

    /**
     * Create an instance of {@link OrderedStpType }
     * 
     */
    public OrderedStpType createOrderedStpType() {
        return new OrderedStpType();
    }

    /**
     * Create an instance of {@link GenericFailedType }
     * 
     */
    public GenericFailedType createGenericFailedType() {
        return new GenericFailedType();
    }

    /**
     * Create an instance of {@link SummaryPathType }
     * 
     */
    public SummaryPathType createSummaryPathType() {
        return new SummaryPathType();
    }

    /**
     * Create an instance of {@link QueryType }
     * 
     */
    public QueryType createQueryType() {
        return new QueryType();
    }

    /**
     * Create an instance of {@link StpType }
     * 
     */
    public StpType createStpType() {
        return new StpType();
    }

    /**
     * Create an instance of {@link ScheduleType }
     * 
     */
    public ScheduleType createScheduleType() {
        return new ScheduleType();
    }

    /**
     * Create an instance of {@link ProvisionStateType }
     * 
     */
    public ProvisionStateType createProvisionStateType() {
        return new ProvisionStateType();
    }

    /**
     * Create an instance of {@link ReserveConfirmedType }
     * 
     */
    public ReserveConfirmedType createReserveConfirmedType() {
        return new ReserveConfirmedType();
    }

    /**
     * Create an instance of {@link ChildSummaryListType }
     * 
     */
    public ChildSummaryListType createChildSummaryListType() {
        return new ChildSummaryListType();
    }

    /**
     * Create an instance of {@link ReservationRequestCriteriaType }
     * 
     */
    public ReservationRequestCriteriaType createReservationRequestCriteriaType() {
        return new ReservationRequestCriteriaType();
    }

    /**
     * Create an instance of {@link ModifyRequestCriteriaType }
     * 
     */
    public ModifyRequestCriteriaType createModifyRequestCriteriaType() {
        return new ModifyRequestCriteriaType();
    }

    /**
     * Create an instance of {@link ConnectionStatesType }
     * 
     */
    public ConnectionStatesType createConnectionStatesType() {
        return new ConnectionStatesType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericConfirmedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "modifyCancelConfirmed")
    public JAXBElement<GenericConfirmedType> createModifyCancelConfirmed(GenericConfirmedType value) {
        return new JAXBElement<GenericConfirmedType>(_ModifyCancelConfirmed_QNAME, GenericConfirmedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "modify")
    public JAXBElement<GenericRequestType> createModify(GenericRequestType value) {
        return new JAXBElement<GenericRequestType>(_Modify_QNAME, GenericRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericFailedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "reserveFailed")
    public JAXBElement<GenericFailedType> createReserveFailed(GenericFailedType value) {
        return new JAXBElement<GenericFailedType>(_ReserveFailed_QNAME, GenericFailedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericConfirmedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "provisionConfirmed")
    public JAXBElement<GenericConfirmedType> createProvisionConfirmed(GenericConfirmedType value) {
        return new JAXBElement<GenericConfirmedType>(_ProvisionConfirmed_QNAME, GenericConfirmedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "provision")
    public JAXBElement<GenericRequestType> createProvision(GenericRequestType value) {
        return new JAXBElement<GenericRequestType>(_Provision_QNAME, GenericRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link NotificationRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "notification")
    public JAXBElement<NotificationRequestType> createNotification(NotificationRequestType value) {
        return new JAXBElement<NotificationRequestType>(_Notification_QNAME, NotificationRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericFailedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "modifyCancelFailed")
    public JAXBElement<GenericFailedType> createModifyCancelFailed(GenericFailedType value) {
        return new JAXBElement<GenericFailedType>(_ModifyCancelFailed_QNAME, GenericFailedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "modifyCancel")
    public JAXBElement<GenericRequestType> createModifyCancel(GenericRequestType value) {
        return new JAXBElement<GenericRequestType>(_ModifyCancel_QNAME, GenericRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericFailedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "releaseFailed")
    public JAXBElement<GenericFailedType> createReleaseFailed(GenericFailedType value) {
        return new JAXBElement<GenericFailedType>(_ReleaseFailed_QNAME, GenericFailedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericConfirmedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "terminateConfirmed")
    public JAXBElement<GenericConfirmedType> createTerminateConfirmed(GenericConfirmedType value) {
        return new JAXBElement<GenericConfirmedType>(_TerminateConfirmed_QNAME, GenericConfirmedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryFailedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "queryFailed")
    public JAXBElement<QueryFailedType> createQueryFailed(QueryFailedType value) {
        return new JAXBElement<QueryFailedType>(_QueryFailed_QNAME, QueryFailedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericConfirmedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "releaseConfirmed")
    public JAXBElement<GenericConfirmedType> createReleaseConfirmed(GenericConfirmedType value) {
        return new JAXBElement<GenericConfirmedType>(_ReleaseConfirmed_QNAME, GenericConfirmedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ModifyCheckType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "modifyCheck")
    public JAXBElement<ModifyCheckType> createModifyCheck(ModifyCheckType value) {
        return new JAXBElement<ModifyCheckType>(_ModifyCheck_QNAME, ModifyCheckType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "terminate")
    public JAXBElement<GenericRequestType> createTerminate(GenericRequestType value) {
        return new JAXBElement<GenericRequestType>(_Terminate_QNAME, GenericRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "query")
    public JAXBElement<QueryType> createQuery(QueryType value) {
        return new JAXBElement<QueryType>(_Query_QNAME, QueryType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReserveType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "reserve")
    public JAXBElement<ReserveType> createReserve(ReserveType value) {
        return new JAXBElement<ReserveType>(_Reserve_QNAME, ReserveType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericFailedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "modifyFailed")
    public JAXBElement<GenericFailedType> createModifyFailed(GenericFailedType value) {
        return new JAXBElement<GenericFailedType>(_ModifyFailed_QNAME, GenericFailedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericFailedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "modifyCheckFailed")
    public JAXBElement<GenericFailedType> createModifyCheckFailed(GenericFailedType value) {
        return new JAXBElement<GenericFailedType>(_ModifyCheckFailed_QNAME, GenericFailedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericRequestType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "release")
    public JAXBElement<GenericRequestType> createRelease(GenericRequestType value) {
        return new JAXBElement<GenericRequestType>(_Release_QNAME, GenericRequestType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ModifyCheckConfirmedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "modifyCheckConfirmed")
    public JAXBElement<ModifyCheckConfirmedType> createModifyCheckConfirmed(ModifyCheckConfirmedType value) {
        return new JAXBElement<ModifyCheckConfirmedType>(_ModifyCheckConfirmed_QNAME, ModifyCheckConfirmedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ReserveConfirmedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "reserveConfirmed")
    public JAXBElement<ReserveConfirmedType> createReserveConfirmed(ReserveConfirmedType value) {
        return new JAXBElement<ReserveConfirmedType>(_ReserveConfirmed_QNAME, ReserveConfirmedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link QueryConfirmedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "queryConfirmed")
    public JAXBElement<QueryConfirmedType> createQueryConfirmed(QueryConfirmedType value) {
        return new JAXBElement<QueryConfirmedType>(_QueryConfirmed_QNAME, QueryConfirmedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericConfirmedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "modifyConfirmed")
    public JAXBElement<GenericConfirmedType> createModifyConfirmed(GenericConfirmedType value) {
        return new JAXBElement<GenericConfirmedType>(_ModifyConfirmed_QNAME, GenericConfirmedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericFailedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "provisionFailed")
    public JAXBElement<GenericFailedType> createProvisionFailed(GenericFailedType value) {
        return new JAXBElement<GenericFailedType>(_ProvisionFailed_QNAME, GenericFailedType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GenericFailedType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://schemas.ogf.org/nsi/2012/03/connection/types", name = "terminateFailed")
    public JAXBElement<GenericFailedType> createTerminateFailed(GenericFailedType value) {
        return new JAXBElement<GenericFailedType>(_TerminateFailed_QNAME, GenericFailedType.class, null, value);
    }

}
