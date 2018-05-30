import weka.core.SparseInstance;

public class MySparseInstance extends SparseInstance{

    public MySparseInstance(int numAttributes) {

        m_AttValues = new double[numAttributes];
        m_NumAttributes = numAttributes;
        m_Indices = new int[numAttributes];
        for (int i = 0; i < m_AttValues.length; i++) {
            m_AttValues[i] = 0.0D; // This is the modified line. Normally it's set to Utils.missingValue()
            m_Indices[i] = i;
        }
        m_Weight = 1;
        m_Dataset = null;
    }
}
