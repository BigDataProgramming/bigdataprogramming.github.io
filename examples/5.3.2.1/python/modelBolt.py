import pickle

import numpy as np
import storm


class modelBolt(storm.BasicBolt):
    base_path = "/opt/examples/5.3.2.1/python/models"
    output_path = "/opt/examples/5.3.2.1/"

    model = pickle.load(open(f"{base_path}/rf_model.pkl", 'rb'))
    label_enc = pickle.load(open(f"{base_path}/label_encoder_targets.pkl", 'rb'))

    def initialize(self, conf, context):
        self._conf = conf
        self._context = context

    def process(self, tuple):
        prediction = self.model.predict(np.reshape(tuple.values, (1, -1)))[0]
        storm.emit([int(prediction)])
        f = open(f"{self.output_path}/results.txt", "a")
        f.write(str(self.label_enc.inverse_transform((np.reshape(prediction, (-1,1))))[0]) + "\n")
        f.close()


modelBolt().run()
