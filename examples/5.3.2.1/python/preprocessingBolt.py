import pickle
from customLabelEncoder import LabelEncoderExt
import numpy as np
import storm
import sys


class preprocessingBolt(storm.BasicBolt):
    # nominal cols
    base_path = "/opt/examples/5.3.2.1/python/models"
    label_encoder_flag = pickle.load(
        open(f"{base_path}/label_encoder_flag.pkl", 'rb'))
    label_encoder_prot = pickle.load(open(f"{base_path}/label_encoder_protocol_type.pkl", 'rb'))
    label_encoder_service = pickle.load(open(f"{base_path}/label_encoder_service.pkl", 'rb'))

    # numeric cols
    standard_scaler = pickle.load(open(f"{base_path}/standard_scaler.pkl", 'rb'))

    nominal_idx, binary_idx = [1, 2, 3], [6, 11, 13, 14, 20, 21]
    numeric_idx = list(set(range(41)).difference(nominal_idx).difference(binary_idx))

    def initialize(self, conf, context):
        self._conf = conf
        self._context = context

    def process(self, tuple):
        protocol_type = tuple.values[self.nominal_idx[0]]  # protocol_type
        protocol_type = self.label_encoder_prot.transform([str(protocol_type)])[0]

        service = tuple.values[self.nominal_idx[1]]  # service
        service = self.label_encoder_service.transform([str(service)])[0]

        flag = tuple.values[self.nominal_idx[2]]  # flag
        flag = self.label_encoder_flag.transform([str(flag)])[0]

        scaled_features = self.standard_scaler.transform(
            np.reshape([float(tuple.values[i]) for i in self.numeric_idx], (1, -1)))[0]

        storm.emit([float(scaled_features[0]),
                    int(protocol_type),
                    int(service),
                    int(flag),
                    float(scaled_features[1]),
                    float(scaled_features[2]),
                    float(tuple.values[self.binary_idx[0]]),
                    float(scaled_features[3]),
                    float(scaled_features[4]),
                    float(scaled_features[5]),
                    float(scaled_features[6]),
                    float(tuple.values[self.binary_idx[1]]),
                    float(scaled_features[7]),
                    float(tuple.values[self.binary_idx[2]]),
                    float(tuple.values[self.binary_idx[3]]),
                    float(scaled_features[8]),
                    float(scaled_features[9]),
                    float(scaled_features[10]),
                    float(scaled_features[11]),
                    float(scaled_features[12]),
                    float(tuple.values[self.binary_idx[4]]),
                    float(tuple.values[self.binary_idx[5]]),
                    float(scaled_features[13]),
                    float(scaled_features[14]),
                    float(scaled_features[15]),
                    float(scaled_features[16]),
                    float(scaled_features[17]),
                    float(scaled_features[18]),
                    float(scaled_features[19]),
                    float(scaled_features[20]),
                    float(scaled_features[21]),
                    float(scaled_features[22]),
                    float(scaled_features[23]),
                    float(scaled_features[24]),
                    float(scaled_features[25]),
                    float(scaled_features[26]),
                    float(scaled_features[27]),
                    float(scaled_features[28]),
                    float(scaled_features[29]),
                    float(scaled_features[30]),
                    float(scaled_features[31])
                    ])

preprocessingBolt().run()
