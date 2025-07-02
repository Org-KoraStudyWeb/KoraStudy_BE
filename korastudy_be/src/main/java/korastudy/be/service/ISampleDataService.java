package korastudy.be.service;

public interface ISampleDataService {
    
    /**
     * Create sample mock test data
     */
    void createSampleMockTests();
    
    /**
     * Create sample user data and test results
     */
    void createSampleUsers();
    
    /**
     * Create all sample data
     */
    void createAllSampleData();
    
    /**
     * Clear all sample data
     */
    void clearSampleData();
}
