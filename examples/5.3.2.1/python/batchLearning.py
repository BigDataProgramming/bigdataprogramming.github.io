import pickle
import warnings

import numpy as np
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from customLabelEncoder import LabelEncoderExt
from sklearn.metrics import accuracy_score, confusion_matrix, classification_report

warnings.filterwarnings('ignore')
from sklearn.ensemble import RandomForestClassifier

base_path = "/opt/examples/5.3.2.1/python/models"
input_path = "/opt/examples/5.3.2.1/data"


header_names = ['duration', 'protocol_type', 'service', 'flag', 'src_bytes', 'dst_bytes', 'land', 'wrong_fragment',
                'urgent', 'hot', 'num_failed_logins', 'logged_in', 'num_compromised', 'root_shell', 'su_attempted',
                'num_root', 'num_file_creations', 'num_shells', 'num_access_files', 'num_outbound_cmds',
                'is_host_login', 'is_guest_login', 'count', 'srv_count', 'serror_rate', 'srv_serror_rate',
                'rerror_rate', 'srv_rerror_rate', 'same_srv_rate', 'diff_srv_rate', 'srv_diff_host_rate',
                'dst_host_count', 'dst_host_srv_count', 'dst_host_same_srv_rate', 'dst_host_diff_srv_rate',
                'dst_host_same_src_port_rate', 'dst_host_srv_diff_host_rate', 'dst_host_serror_rate',
                'dst_host_srv_serror_rate', 'dst_host_rerror_rate', 'dst_host_srv_rerror_rate', 'attack_type']
col_names = np.array(header_names)

nominal_idx = [1, 2, 3]
binary_idx = [6, 11, 13, 14, 20, 21]
numeric_idx = list(set(range(41)).difference(nominal_idx).difference(binary_idx))

nominal_cols = col_names[nominal_idx].tolist()
binary_cols = col_names[binary_idx].tolist()
numeric_cols = col_names[numeric_idx].tolist()

X = pd.read_csv(f"{input_path}/kddcup_train_10.csv", names=header_names)
y = X['attack_type']
X = X.drop(['attack_type'], axis=1)

X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.20, random_state=42)
X_train[nominal_cols] = X_train[nominal_cols].astype('category')

# preprocessing
standard_scaler = StandardScaler().fit(X_train[numeric_cols])

X_train[numeric_cols] = standard_scaler.transform(X_train[numeric_cols])
X_test[numeric_cols] = standard_scaler.transform(X_test[numeric_cols])

pickle.dump(standard_scaler, open(f"{base_path}/standard_scaler.pkl", 'wb'))

for col in nominal_cols:
    le = LabelEncoderExt().fit(X_train[col])
    pickle.dump(le, open(f"{base_path}/label_encoder_" + col + ".pkl", 'wb'))
    X_train[col] = le.transform(X_train[col])
    X_test[col] = le.transform(X_test[col])

label_encoder_y = LabelEncoderExt().fit(y_train)
y_train = label_encoder_y.transform(y_train)
pickle.dump(label_encoder_y, open(f"{base_path}/label_encoder_targets.pkl", 'wb'))
y_test = label_encoder_y.transform(y_test)

# model
rf = RandomForestClassifier()

rf.fit(X_train, y_train)
pickle.dump(rf, open(f"{base_path}/rf_model.pkl", 'wb'))
y_pred = rf.predict(X_test)


print(accuracy_score(y_pred, y_test))
print(classification_report(y_pred, y_test))
